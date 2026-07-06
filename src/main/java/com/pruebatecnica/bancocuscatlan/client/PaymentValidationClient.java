package com.pruebatecnica.bancocuscatlan.client;

import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;

public interface PaymentValidationClient {

    PaymentValidationResponse validate(PaymentValidationRequest request);
}
