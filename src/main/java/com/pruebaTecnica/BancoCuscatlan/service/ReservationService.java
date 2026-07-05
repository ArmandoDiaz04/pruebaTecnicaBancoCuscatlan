package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Reservation;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.User;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateReservationRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationResponse;
import com.pruebaTecnica.BancoCuscatlan.exception.BadRequestException;
import com.pruebaTecnica.BancoCuscatlan.exception.ConflictException;
import com.pruebaTecnica.BancoCuscatlan.exception.ForbiddenException;
import com.pruebaTecnica.BancoCuscatlan.exception.ResourceNotFoundException;
import com.pruebaTecnica.BancoCuscatlan.mapper.ReservationMapper;
import com.pruebaTecnica.BancoCuscatlan.repository.ReservationRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.SpaceRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.UserRepository;
import com.pruebaTecnica.BancoCuscatlan.security.AuthenticatedUserPrincipal;
import com.pruebaTecnica.BancoCuscatlan.security.SecurityUtils;
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

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            SpaceRepository spaceRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.spaceRepository = spaceRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        AuthenticatedUserPrincipal principal = SecurityUtils.currentUser();
        if (principal.role() == Role.USER && !principal.id().equals(request.getUserId())) {
            throw new ForbiddenException("No puede crear reservas para otro usuario");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.getUserId()));

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
            throw new ConflictException("El espacio ya tiene una reserva en el rango de tiempo solicitado");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .status(ReservationStatus.PENDING_PAYMENT)
                .paymentReference(request.getPaymentReference())
                .totalAmount(calculateTotalAmount(space.getHourlyRate(), request.getStartDateTime(), request.getEndDateTime()))
                .build();

        Reservation created = reservationRepository.save(reservation);
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
            throw new ForbiddenException("No puede consultar reservas de otro usuario");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + userId);
        }

        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::toResponse)
                .toList();
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
            throw new ForbiddenException("No puede cancelar reservas de otro usuario");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("La reserva ya está cancelada");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updated);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + reservationId));

        reservation.setStatus(status);
        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toResponse(updated);
    }

    private BigDecimal calculateTotalAmount(BigDecimal hourlyRate, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
