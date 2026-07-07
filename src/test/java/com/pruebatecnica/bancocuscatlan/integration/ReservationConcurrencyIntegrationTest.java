package com.pruebatecnica.bancocuscatlan.integration;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.domain.entity.Space;
import com.pruebatecnica.bancocuscatlan.domain.entity.User;
import com.pruebatecnica.bancocuscatlan.domain.enums.Role;
import com.pruebatecnica.bancocuscatlan.domain.enums.SpaceType;
import com.pruebatecnica.bancocuscatlan.dto.CreateReservationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Demuestra que el EXCLUDE constraint de la migración V3 (no la lógica de
 * aplicación en READ COMMITTED) es lo que impide que dos requests
 * concurrentes al mismo slot terminen ambas persistidas y solapadas.
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyIntegrationTest {

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
        reservationRepository.deleteAll();
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void concurrentOverlappingRequests_onlyOneSucceeds_dbConstraintBlocksRace() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Usuario Concurrencia")
                .email("concurrencia@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build());

        Space space = spaceRepository.save(Space.builder()
                .name("Sala Concurrencia")
                .type(SpaceType.MEETING_ROOM)
                .capacity(5)
                .location("Nivel 1")
                .hourlyRate(new BigDecimal("50.00"))
                .active(true)
                .build());

        when(paymentValidationService.validatePayment(any())).thenReturn(CompletableFuture.completedFuture(
                PaymentValidationResponse.builder()
                        .approved(true)
                        .transactionId("tx-concurrency")
                        .message("approved")
                        .build()
        ));

        LocalDateTime start = LocalDateTime.of(2026, Month.AUGUST, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int idx = i;
            Callable<Boolean> task = () -> {
                AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(user.getId(), user.getEmail(), Role.USER);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, "n/a", principal.authorities()));
                CreateReservationRequest request = new CreateReservationRequest(
                        null, space.getId(), start, end, null, "card-" + idx);
                try {
                    barrier.await(10, TimeUnit.SECONDS);
                    reservationService.createReservation(request);
                    return true;
                } catch (OverlappingReservationException ex) {
                    return false;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
            futures.add(executor.submit(task));
        }

        long successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(15, TimeUnit.SECONDS)) {
                successCount++;
            }
        }
        executor.shutdown();

        assertThat(successCount).isEqualTo(1);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }
}
