package com.pruebatecnica.bancocuscatlan.dto;

import com.pruebatecnica.bancocuscatlan.domain.enums.ReservationStatus;
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
public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long spaceId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ReservationStatus status;
    private BigDecimal totalAmount;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
