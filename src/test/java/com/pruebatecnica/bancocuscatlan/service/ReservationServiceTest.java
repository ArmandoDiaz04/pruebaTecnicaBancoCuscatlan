package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.entity.Reservation;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.User;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.SpaceType;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateReservationRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationResponse;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationResponse;
import com.pruebaTecnica.BancoCuscatlan.event.ReservationConfirmedEvent;
import com.pruebaTecnica.BancoCuscatlan.event.ReservationStatusChangedEvent;
import com.pruebaTecnica.BancoCuscatlan.exception.OverlappingReservationException;
import com.pruebaTecnica.BancoCuscatlan.mapper.ReservationMapper;
import com.pruebaTecnica.BancoCuscatlan.repository.ReservationRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.SpaceRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.UserRepository;
import com.pruebaTecnica.BancoCuscatlan.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private PaymentValidationService paymentValidationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReservation_confirmsReservationAndPublishesEvents() {
        authenticate(1L, Role.USER);

        User user = User.builder()
                .id(1L)
                .name("Ana Perez")
                .email("ana@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        Space space = Space.builder()
                .id(2L)
                .name("Sala 1")
                .type(SpaceType.MEETING_ROOM)
                .capacity(8)
                .location("Nivel 3")
                .hourlyRate(new BigDecimal("100.00"))
                .active(true)
                .build();
        CreateReservationRequest request = new CreateReservationRequest(
                null,
                2L,
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 12, 30),
                null,
                "card-123"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(2L)).thenReturn(Optional.of(space));
        when(reservationRepository.existsOverlappingReservation(eq(2L), any(), any(), any())).thenReturn(false);
        when(paymentValidationService.validatePayment(any())).thenReturn(CompletableFuture.completedFuture(
                PaymentValidationResponse.builder()
                        .approved(true)
                        .transactionId("tx-123")
                        .message("approved")
                        .build()
        ));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(10L);
            return reservation;
        });
        when(reservationMapper.toResponse(any(Reservation.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ReservationResponse response = reservationService.createReservation(request);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        Reservation saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(saved.getPaymentReference()).isEqualTo("tx-123");
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(eventPublisher).publishEvent(any(ReservationConfirmedEvent.class));
        verify(eventPublisher).publishEvent(any(ReservationStatusChangedEvent.class));
    }

    @Test
    void createReservation_rejectsOverlappingReservation() {
        authenticate(1L, Role.USER);

        User user = User.builder()
                .id(1L)
                .name("Ana Perez")
                .email("ana@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        Space space = Space.builder()
                .id(2L)
                .name("Sala 1")
                .type(SpaceType.MEETING_ROOM)
                .capacity(8)
                .location("Nivel 3")
                .hourlyRate(new BigDecimal("100.00"))
                .active(true)
                .build();
        CreateReservationRequest request = new CreateReservationRequest(
                null,
                2L,
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 12, 0),
                null,
                "card-123"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(2L)).thenReturn(Optional.of(space));
        when(reservationRepository.existsOverlappingReservation(eq(2L), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(OverlappingReservationException.class);

        verifyNoInteractions(paymentValidationService);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_marksReservationAsCancelled() {
        authenticate(1L, Role.USER);

        User user = User.builder()
                .id(1L)
                .name("Ana Perez")
                .email("ana@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        Space space = Space.builder()
                .id(2L)
                .name("Sala 1")
                .type(SpaceType.MEETING_ROOM)
                .capacity(8)
                .location("Nivel 3")
                .hourlyRate(new BigDecimal("100.00"))
                .active(true)
                .build();
        Reservation reservation = Reservation.builder()
                .id(11L)
                .user(user)
                .space(space)
                .startDateTime(LocalDateTime.of(2026, 7, 6, 10, 0))
                .endDateTime(LocalDateTime.of(2026, 7, 6, 12, 0))
                .status(ReservationStatus.CONFIRMED)
                .totalAmount(new BigDecimal("200.00"))
                .paymentReference("tx-123")
                .build();

        when(reservationRepository.findById(11L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));

        ReservationResponse response = reservationService.cancelReservation(11L);

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(eventPublisher).publishEvent(any(ReservationStatusChangedEvent.class));
    }

    private void authenticate(Long id, Role role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(id, "user@example.com", role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                "n/a",
                principal.authorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .spaceId(reservation.getSpace().getId())
                .startDateTime(reservation.getStartDateTime())
                .endDateTime(reservation.getEndDateTime())
                .status(reservation.getStatus())
                .totalAmount(reservation.getTotalAmount())
                .paymentReference(reservation.getPaymentReference())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}