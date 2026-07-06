package com.pruebatecnica.bancocuscatlan.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    private Long userId;

    @NotNull(message = "El espacio es obligatorio")
    private Long spaceId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha y hora de inicio debe ser futura")
    private LocalDateTime startDateTime;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @Future(message = "La fecha y hora de fin debe ser futura")
    private LocalDateTime endDateTime;

    private String paymentReference;

    private String paymentMethodId;
}
