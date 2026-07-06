package com.pruebatecnica.bancocuscatlan.dto;

import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private ReservationStatus status;
}
