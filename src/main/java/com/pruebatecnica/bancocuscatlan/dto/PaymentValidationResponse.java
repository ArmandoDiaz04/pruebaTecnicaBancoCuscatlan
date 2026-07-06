package com.pruebatecnica.bancocuscatlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidationResponse {

    private boolean approved;
    private String transactionId;
    private String message;
}
