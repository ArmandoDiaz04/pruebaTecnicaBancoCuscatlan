package com.pruebatecnica.bancocuscatlan.controller;

import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/mock/payments")
@Tag(name = "Mock Payments", description = "Simulación de servicio externo de pagos")
public class MockPaymentController {

    @PostMapping("/validate")
    @Operation(summary = "Validar pago (mock)")
    public ResponseEntity<PaymentValidationResponse> validatePayment(@Valid @RequestBody PaymentValidationRequest request) {
        String method = request.getPaymentMethodId() == null ? "" : request.getPaymentMethodId().toLowerCase();

        if (method.contains("reject") || method.contains("fail")) {
            return ResponseEntity.ok(PaymentValidationResponse.builder()
                    .approved(false)
                    .transactionId("txn_rejected_" + UUID.randomUUID())
                    .message("Pago rechazado por el validador mock")
                    .build());
        }

        return ResponseEntity.ok(PaymentValidationResponse.builder()
                .approved(true)
                .transactionId("txn_" + UUID.randomUUID())
                .message("Pago aprobado")
                .build());
    }
}
