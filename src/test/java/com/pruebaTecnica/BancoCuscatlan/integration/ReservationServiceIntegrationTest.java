package com.pruebaTecnica.BancoCuscatlan.integration;

import com.pruebaTecnica.BancoCuscatlan.TestcontainersConfiguration;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.Space;
import com.pruebaTecnica.BancoCuscatlan.domain.entity.User;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.Role;
import com.pruebaTecnica.BancoCuscatlan.domain.enums.SpaceType;
import com.pruebaTecnica.BancoCuscatlan.dto.CreateReservationRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationResponse;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationResponse;
import com.pruebaTecnica.BancoCuscatlan.exception.OverlappingReservationException;
import com.pruebaTecnica.BancoCuscatlan.repository.ReservationRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.SpaceRepository;
import com.pruebaTecnica.BancoCuscatlan.repository.UserRepository;
import com.pruebaTecnica.BancoCuscatlan.security.AuthenticatedUserPrincipal;
import com.pruebaTecnica.BancoCuscatlan.service.PaymentValidationService;
import com.pruebaTecnica.BancoCuscatlan.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
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

    @MockBean
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
                LocalDateTime.of(2026, 7, 6, 10, 0),
                LocalDateTime.of(2026, 7, 6, 12, 0),
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
                LocalDateTime.of(2026, 7, 6, 11, 0),
                LocalDateTime.of(2026, 7, 6, 13, 0),
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