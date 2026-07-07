package com.pruebatecnica.bancocuscatlan.service;

import com.pruebatecnica.bancocuscatlan.client.PaymentValidationClient;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentValidationService {

    private final PaymentValidationClient paymentValidationClient;

    public PaymentValidationService(PaymentValidationClient paymentValidationClient) {
        this.paymentValidationClient = paymentValidationClient;
    }

    // El fallback se declara solo en @CircuitBreaker (aspecto más externo, por
    // el orden por defecto de Resilience4j: CircuitBreaker > TimeLimiter). Si
    // también se declarara en @TimeLimiter, su fallback interceptaría la
    // excepción antes de que CircuitBreaker la viera, y el circuito nunca
    // registraría fallos ni abriría (bug real detectado al agregar el test
    // que verifica el estado OPEN del circuito).
    @CircuitBreaker(name = "paymentValidation", fallbackMethod = "fallbackValidation")
    @TimeLimiter(name = "paymentValidation")
    public CompletableFuture<PaymentValidationResponse> validatePayment(PaymentValidationRequest request) {
        return CompletableFuture.supplyAsync(() -> paymentValidationClient.validate(request));
    }

    private CompletableFuture<PaymentValidationResponse> fallbackValidation(PaymentValidationRequest request, Throwable throwable) {
        return CompletableFuture.completedFuture(PaymentValidationResponse.builder()
                .approved(false)
                .transactionId("pending_" + UUID.randomUUID())
                .message("Fallback de pago aplicado. Reserva queda en PENDING_PAYMENT")
                .build());
    }
}
