package com.pruebatecnica.bancocuscatlan.service;

import com.pruebatecnica.bancocuscatlan.TestcontainersConfiguration;
import com.pruebatecnica.bancocuscatlan.client.PaymentValidationClient;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.profiles.active=test")
@Import(TestcontainersConfiguration.class)
class PaymentValidationServiceTest {

    @MockitoBean
    private PaymentValidationClient paymentValidationClient;

    @Autowired
    private PaymentValidationService paymentValidationService;

    @Test
    void validatePayment_returnsApprovedResponseWhenClientSucceeds() {
        when(paymentValidationClient.validate(any())).thenReturn(PaymentValidationResponse.builder()
                .approved(true)
                .transactionId("tx-001")
                .message("approved")
                .build());

        PaymentValidationResponse response = paymentValidationService.validatePayment(buildRequest()).join();

        assertThat(response.isApproved()).isTrue();
        assertThat(response.getTransactionId()).isEqualTo("tx-001");
        assertThat(response.getMessage()).isEqualTo("approved");
        verify(paymentValidationClient, times(1)).validate(any());
    }

    @Test
    void validatePayment_usesFallbackWhenClientFails() {
        when(paymentValidationClient.validate(any())).thenThrow(new IllegalStateException("boom"));

        PaymentValidationResponse response = paymentValidationService.validatePayment(buildRequest()).join();

        assertThat(response.isApproved()).isFalse();
        assertThat(response.getTransactionId()).startsWith("pending_");
        assertThat(response.getMessage()).contains("PENDING_PAYMENT");
        verify(paymentValidationClient, times(1)).validate(any());
    }

    private PaymentValidationRequest buildRequest() {
        return PaymentValidationRequest.builder()
                .paymentMethodId("card-001")
                .amount(new BigDecimal("150.00"))
                .reservationId(1L)
                .build();
    }
}