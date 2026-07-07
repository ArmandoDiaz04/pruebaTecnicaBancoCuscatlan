package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.client.PaymentValidationClient;
import com.pruebatecnica.bancocuscatlan.domain.entity.Space;
import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.domain.enums.SpaceType;
import com.pruebatecnica.bancocuscatlan.dto.CreateReservationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.ReservationResponse;
import com.pruebatecnica.bancocuscatlan.repository.ReservationRepository;
import com.pruebatecnica.bancocuscatlan.repository.SpaceRepository;
import com.pruebatecnica.bancocuscatlan.repository.UserRepository;
import com.pruebatecnica.bancocuscatlan.security.AuthenticatedUserPrincipal;
import com.pruebatecnica.bancocuscatlan.service.PaymentValidationService;
import com.pruebatecnica.bancocuscatlan.service.ReservationService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el estado OPEN real del circuit breaker (no solo el fallback de
 * negocio ante un fallo aislado, ya cubierto por PaymentValidationServiceTest).
 * Sobreescribe umbrales solo para este test, para que la apertura sea
 * determinística sin depender de los valores de dev/prod.
 */
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "resilience4j.circuitbreaker.instances.paymentValidation.sliding-window-size=4",
        "resilience4j.circuitbreaker.instances.paymentValidation.minimum-number-of-calls=4",
        "resilience4j.circuitbreaker.instances.paymentValidation.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.paymentValidation.wait-duration-in-open-state=10s",
        "resilience4j.circuitbreaker.instances.paymentValidation.automatic-transition-from-open-to-half-open-enabled=false"
})
@Import(TestcontainersConfiguration.class)
class PaymentCircuitBreakerIntegrationTest {

    @Autowired
    private PaymentValidationService paymentValidationService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    private PaymentValidationClient paymentValidationClient;

    @AfterEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("paymentValidation").reset();
        SecurityContextHolder.clearContext();
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void repeatedFailures_openCircuitBreaker_andReservationFallsBackToPendingPayment() {
        when(paymentValidationClient.validate(any())).thenThrow(new RuntimeException("payment gateway down"));

        for (int i = 0; i < 4; i++) {
            var response = paymentValidationService.validatePayment(buildRequest()).join();
            assertThat(response.isApproved()).isFalse();
        }

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentValidation");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        User user = userRepository.save(User.builder()
                .name("CB Test").email("cb-test@example.com")
                .password("encoded-password").role(Role.USER).build());
        Space space = spaceRepository.save(Space.builder()
                .name("Sala CB").type(SpaceType.MEETING_ROOM).capacity(5)
                .location("Nivel 2").hourlyRate(new BigDecimal("50.00")).active(true).build());
        authenticate(user.getId());

        CreateReservationRequest request = new CreateReservationRequest(
                null, space.getId(),
                LocalDateTime.of(2026, Month.SEPTEMBER, 1, 10, 0),
                LocalDateTime.of(2026, Month.SEPTEMBER, 1, 12, 0),
                null, "card-cb");

        ReservationResponse reservation = reservationService.createReservation(request);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        // Con el circuito ya OPEN, esta 5a llamada corta directo al fallback: el
        // cliente HTTP real sigue habiendo sido invocado solo las 4 veces previas.
        verify(paymentValidationClient, times(4)).validate(any());
    }

    private PaymentValidationRequest buildRequest() {
        return PaymentValidationRequest.builder()
                .paymentMethodId("card-001").amount(new BigDecimal("100.00")).reservationId(null).build();
    }

    private void authenticate(Long userId) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(userId, "cb-test@example.com", Role.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "n/a", principal.authorities()));
    }
}
