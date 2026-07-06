package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.client.PaymentValidationClient;
import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationResponse;
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

    @CircuitBreaker(name = "paymentValidation", fallbackMethod = "fallbackValidation")
    @TimeLimiter(name = "paymentValidation", fallbackMethod = "fallbackValidation")
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
