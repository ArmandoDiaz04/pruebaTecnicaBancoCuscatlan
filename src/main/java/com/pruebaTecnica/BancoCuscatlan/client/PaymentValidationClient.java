package com.pruebaTecnica.BancoCuscatlan.client;

import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.PaymentValidationResponse;

public interface PaymentValidationClient {

    PaymentValidationResponse validate(PaymentValidationRequest request);
}
