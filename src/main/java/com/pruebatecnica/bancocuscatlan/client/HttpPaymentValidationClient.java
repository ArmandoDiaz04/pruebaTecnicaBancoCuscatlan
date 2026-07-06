package com.pruebatecnica.bancocuscatlan.client;

import com.pruebatecnica.bancocuscatlan.config.AppProperties;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationRequest;
import com.pruebatecnica.bancocuscatlan.dto.PaymentValidationResponse;
import com.pruebatecnica.bancocuscatlan.exception.PaymentValidationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpPaymentValidationClient implements PaymentValidationClient {

    private final RestClient restClient;
    private final AppProperties appProperties;

    public HttpPaymentValidationClient(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = RestClient.builder()
                .baseUrl(appProperties.getPayment().getBaseUrl())
                .build();
    }

    @Override
    public PaymentValidationResponse validate(PaymentValidationRequest request) {
        try {
            PaymentValidationResponse response = restClient.post()
                    .uri(appProperties.getPayment().getValidatePath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PaymentValidationResponse.class);

            if (response == null) {
                throw new PaymentValidationException("Respuesta vacía del validador de pago");
            }
            return response;
        } catch (RestClientException ex) {
            throw new PaymentValidationException("No se pudo validar el pago con el servicio externo", ex);
        }
    }
}
