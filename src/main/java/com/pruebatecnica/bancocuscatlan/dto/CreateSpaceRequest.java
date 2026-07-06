package com.pruebatecnica.bancocuscatlan.dto;

import com.pruebatecnica.bancocuscatlan.domain.enums.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpaceRequest {

    @NotBlank(message = "El nombre del espacio es obligatorio")
    private String name;

    @NotNull(message = "El tipo de espacio es obligatorio")
    private SpaceType type;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacity;

    @NotBlank(message = "La ubicación es obligatoria")
    private String location;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor que 0")
    private BigDecimal hourlyRate;

    private Boolean active;
}
