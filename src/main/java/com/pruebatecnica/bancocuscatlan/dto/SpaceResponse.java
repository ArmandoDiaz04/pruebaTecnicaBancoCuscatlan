package com.pruebatecnica.bancocuscatlan.dto;

import com.pruebatecnica.bancocuscatlan.domain.enums.SpaceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceResponse {

    private Long id;
    private String name;
    private SpaceType type;
    private Integer capacity;
    private String location;
    private BigDecimal hourlyRate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
