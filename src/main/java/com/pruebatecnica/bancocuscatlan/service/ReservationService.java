package com.pruebatecnica.bancocuscatlan.service;

import com.pruebatecnica.bancocuscatlan.domain.entity.Reservation;
import com.pruebatecnica.bancocuscatlan.domain.entity.Space;
import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
import com.pruebatecnica.bancocuscatlan.dto.CreateReservationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import com.pruebatecnica.bancocuscatlan.dto.ReservationResponse;
import com.pruebatecnica.bancocuscatlan.exception.BadRequestException;
import com.pruebatecnica.bancocuscatlan.exception.ForbiddenException;
import com.pruebatecnica.bancocuscatlan.exception.OverlappingReservationException;
import com.pruebatecnica.bancocuscatlan.exception.ResourceNotFoundException;
import com.pruebatecnica.bancocuscatlan.event.ReservationConfirmedEvent;
import com.pruebatecnica.bancocuscatlan.event.ReservationStatusChangedEvent;
import com.pruebatecnica.bancocuscatlan.mapper.ReservationMapper;
import com.pruebatecnica.bancocuscatlan.repository.ReservationRepository;
import com.pruebatecnica.bancocuscatlan.repository.SpaceRepository;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import com.pruebatecnica.bancocuscatlan.security.AuthenticatedUserPrincipal;
import com.pruebatecnica.bancocuscatlan.security.SecurityUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
public class ReservationService {

    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final ReservationMapper reservationMapper;
    private final PaymentValidationService paymentValidationService;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            SpaceRepository spaceRepository,
            ReservationMapper reservationMapper,
            PaymentValidationService paymentValidationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.reservationMapper = reservationMapper;
        this.paymentValidationService = paymentValidationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        AuthenticatedUserPrincipal principal = SecurityUtils.currentUser();

        Long targetUserId = request.getUserId() != null ? request.getUserId() : principal.id();
        if (principal.role() == Role.USER && !principal.id().equals(targetUserId)) {
            throw new ForbiddenException("No puede crear reservas para otro usuario");
        }

        final Long resolvedUserId = targetUserId;
        User user = userRepository.findById(resolvedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + resolvedUserId));

        Space space = spaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + request.getSpaceId()));

        if (!Boolean.TRUE.equals(space.getActive())) {
            throw new BadRequestException("El espacio no está activo para reservas");
        }

        if (!request.getStartDateTime().isBefore(request.getEndDateTime())) {
            throw new BadRequestException("La fecha de inicio debe ser menor que la fecha de fin");
        }

        boolean overlaps = reservationRepository.existsOverlappingReservation(
                space.getId(),
                request.getStartDateTime(),
                request.getEndDateTime(),
                BLOCKING_STATUSES
        );

        if (overlaps) {
            throw new OverlappingReservationException("El espacio ya tiene una reserva en el rango de tiempo solicitado");
        }

        BigDecimal totalAmount = calculateTotalAmount(space.getHourlyRate(), request.getStartDateTime(), request.getEndDateTime());
        PaymentValidationResponse paymentResult = paymentValidationService.validatePayment(
            PaymentValidationRequest.builder()
                .paymentMethodId(resolvePaymentMethodId(request))
                .amount(totalAmount)
                .reservationId(null)
                .build()
        ).join();

        ReservationStatus status = paymentResult.isApproved()
            ? ReservationStatus.CONFIRMED
            : ReservationStatus.PENDING_PAYMENT;

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
            .status(status)
            .paymentReference(resolvePaymentReference(request, paymentResult))
            .totalAmount(totalAmount)
                .build();

        Reservation created;
        try {
            created = reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException ex) {
            // Violación directa del EXCLUDE constraint (V3): la BD detectó el solapamiento.
            throw new OverlappingReservationException("El espacio ya tiene una reserva en el rango de tiempo solicitado");
        } catch (CannotAcquireLockException ex) {
            // Bajo inserciones concurrentes verdaderamente simultáneas, Postgres puede
            // resolver el chequeo del EXCLUDE constraint como un deadlock (en vez de una
            // violación directa) y abortar una de las dos transacciones. La causa raíz es
            // la misma: dos reservas solapadas compitiendo por el mismo rango.
            throw new OverlappingReservationException("El espacio ya tiene una reserva en el rango de tiempo solicitado");
        }
        if (created.getStatus() == ReservationStatus.CONFIRMED) {
            publishConfirmedEvent(created);
        }
        eventPublisher.publishEvent(new ReservationStatusChangedEvent(created.getId(), created.getStatus()));
        return reservationMapper.toResponse(created);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));

        AuthenticatedUserPrincipal principal = SecurityUtils.currentUser();
        if (principal.role() == Role.USER && !reservation.getUser().getId().equals(principal.id())) {
            throw new ForbiddenException("No puede consultar reservas de otro usuario");
        }

        return reservationMapper.toResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUser(Long userId) {
        AuthenticatedUserPrincipal principal = SecurityUtils.currentUser();
        if (principal.role() == Role.USER && !principal.id().equals(userId)) {
            throw new com.pruebatecnica.bancocuscatlan.exception.UnauthorizedReservationAccessException("No puede consultar reservas de otro usuario");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + userId);
        }

        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations() {
        return getReservationsByUser(SecurityUtils.currentUser().id());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + reservationId));

        AuthenticatedUserPrincipal principal = SecurityUtils.currentUser();
        if (principal.role() == Role.USER && !reservation.getUser().getId().equals(principal.id())) {
            throw new com.pruebatecnica.bancocuscatlan.exception.UnauthorizedReservationAccessException("No puede cancelar reservas de otro usuario");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("La reserva ya está cancelada");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);
        eventPublisher.publishEvent(new ReservationStatusChangedEvent(updated.getId(), updated.getStatus()));
        return reservationMapper.toResponse(updated);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long reservationId, ReservationStatus status) {
        if (SecurityUtils.currentUser().role() != Role.ADMIN) {
            throw new ForbiddenException("Solo ADMIN puede actualizar el estado de una reserva");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + reservationId));

        validateStatusTransition(reservation.getStatus(), status);

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);
        if (updated.getStatus() == ReservationStatus.CONFIRMED) {
            publishConfirmedEvent(updated);
        }
        eventPublisher.publishEvent(new ReservationStatusChangedEvent(updated.getId(), updated.getStatus()));
        return reservationMapper.toResponse(updated);
    }

    private String resolvePaymentReference(CreateReservationRequest request, PaymentValidationResponse paymentResult) {
        if (paymentResult.getTransactionId() != null && !paymentResult.getTransactionId().isBlank()) {
            return paymentResult.getTransactionId();
        }
        return request.getPaymentReference();
    }

    private String resolvePaymentMethodId(CreateReservationRequest request) {
        if (request.getPaymentMethodId() != null && !request.getPaymentMethodId().isBlank()) {
            return request.getPaymentMethodId();
        }
        if (request.getPaymentReference() != null && !request.getPaymentReference().isBlank()) {
            return request.getPaymentReference();
        }
        throw new BadRequestException("Debe enviar paymentMethodId o paymentReference");
    }

    private void validateStatusTransition(ReservationStatus currentStatus, ReservationStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case PENDING_PAYMENT -> newStatus == ReservationStatus.CONFIRMED || newStatus == ReservationStatus.CANCELLED;
            case CONFIRMED -> newStatus == ReservationStatus.COMPLETED || newStatus == ReservationStatus.CANCELLED;
            case CANCELLED, COMPLETED -> false;
        };

        if (!valid) {
            throw new com.pruebatecnica.bancocuscatlan.exception.InvalidReservationStateException(
                    "Transición inválida de estado: " + currentStatus + " -> " + newStatus
            );
        }
    }

    private void publishConfirmedEvent(Reservation reservation) {
        eventPublisher.publishEvent(new ReservationConfirmedEvent(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getUser().getName(),
                reservation.getSpace().getId(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime()
        ));
    }

    private BigDecimal calculateTotalAmount(BigDecimal hourlyRate, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
