package com.pruebaTecnica.BancoCuscatlan.dto;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
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
