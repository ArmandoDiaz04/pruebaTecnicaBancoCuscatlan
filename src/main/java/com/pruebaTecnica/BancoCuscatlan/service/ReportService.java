package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationReportResponse;
import com.pruebaTecnica.BancoCuscatlan.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReservationRepository reservationRepository;

    public ReportService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public ReservationReportResponse getReservationSummary() {
        return ReservationReportResponse.builder()
                .totalReservations(reservationRepository.count())
                .pendingPayment(reservationRepository.countByStatus(ReservationStatus.PENDING_PAYMENT))
                .confirmed(reservationRepository.countByStatus(ReservationStatus.CONFIRMED))
                .cancelled(reservationRepository.countByStatus(ReservationStatus.CANCELLED))
                .completed(reservationRepository.countByStatus(ReservationStatus.COMPLETED))
                .build();
    }
}
