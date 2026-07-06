package com.pruebaTecnica.BancoCuscatlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationReportResponse {

    private long totalReservations;
    private long pendingPayment;
    private long confirmed;
    private long cancelled;
    private long completed;
}
