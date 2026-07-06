package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.domain.entity.Space;
import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.domain.enums.SpaceType;
import com.pruebatecnica.bancocuscatlan.dto.CreateReservationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import com.pruebatecnica.bancocuscatlan.dto.ReservationResponse;
import com.pruebatecnica.bancocuscatlan.exception.OverlappingReservationException;
import com.pruebatecnica.bancocuscatlan.repository.ReservationRepository;
import com.pruebatecnica.bancocuscatlan.repository.SpaceRepository;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import com.pruebatecnica.bancocuscatlan.security.AuthenticatedUserPrincipal;
import com.pruebatecnica.bancocuscatlan.service.PaymentValidationService;
import com.pruebatecnica.bancocuscatlan.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Month;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

        @MockitoBean
    private PaymentValidationService paymentValidationService;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReservationThenRejectsOverlapWithRealDatabaseState() {
        User user = userRepository.save(User.builder()
                .name("Usuario Integracion")
                .email("integration@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build());

        Space space = spaceRepository.save(Space.builder()
                .name("Sala Integracion")
                .type(SpaceType.MEETING_ROOM)
                .capacity(10)
                .location("Nivel 4")
                .hourlyRate(new BigDecimal("120.00"))
                .active(true)
                .build());

        authenticate(user.getId(), Role.USER);
        when(paymentValidationService.validatePayment(any())).thenReturn(CompletableFuture.completedFuture(
                PaymentValidationResponse.builder()
                        .approved(true)
                        .transactionId("tx-approval")
                        .message("approved")
                        .build()
        ));

        CreateReservationRequest firstRequest = new CreateReservationRequest(
                null,
                space.getId(),
                LocalDateTime.of(2026, Month.JULY, 6, 10, 0),
                LocalDateTime.of(2026, Month.JULY, 6, 12, 0),
                null,
                "card-001"
        );

        ReservationResponse firstResponse = reservationService.createReservation(firstRequest);

        assertThat(firstResponse.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(firstResponse.getId()).isNotNull();
        assertThat(reservationRepository.count()).isEqualTo(1);

        CreateReservationRequest overlapRequest = new CreateReservationRequest(
                null,
                space.getId(),
                LocalDateTime.of(2026, Month.JULY, 6, 11, 0),
                LocalDateTime.of(2026, Month.JULY, 6, 13, 0),
                null,
                "card-002"
        );

        assertThatThrownBy(() -> reservationService.createReservation(overlapRequest))
                .isInstanceOf(OverlappingReservationException.class);

        assertThat(reservationRepository.count()).isEqualTo(1);
        verify(paymentValidationService, times(1)).validatePayment(any());
    }

    private void authenticate(Long userId, Role role) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "integration@example.com", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "n/a", principal.authorities())
        );
    }
}