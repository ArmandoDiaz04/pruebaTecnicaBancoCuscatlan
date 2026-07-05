package com.pruebaTecnica.BancoCuscatlan.service;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.dto.OccupancyReportResponse;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationReportResponse;
import com.pruebaTecnica.BancoCuscatlan.exception.BadRequestException;
import com.pruebaTecnica.BancoCuscatlan.repository.SpaceRepository;
import org.springframework.cache.annotation.Cacheable;
import com.pruebaTecnica.BancoCuscatlan.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;

    public ReportService(ReservationRepository reservationRepository, SpaceRepository spaceRepository) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
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

    @Cacheable(value = "occupancyReport", key = "#from.toString() + '_' + #to.toString()")
    @Transactional(readOnly = true)
    public List<OccupancyReportResponse> getOccupancyReport(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("El parámetro 'from' debe ser menor o igual a 'to'");
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();
        double totalHours = Duration.between(fromDateTime, toDateTime).toHours();

        return spaceRepository.findByActiveTrue().stream()
                .map(space -> {
                    double reservedHours = reservationRepository
                            .findConfirmedBySpaceAndRange(space.getId(), fromDateTime, toDateTime)
                            .stream()
                            .mapToDouble(reservation -> overlappingHours(
                                    reservation.getStartDateTime(),
                                    reservation.getEndDateTime(),
                                    fromDateTime,
                                    toDateTime
                            ))
                            .sum();

                    double occupancy = totalHours <= 0 ? 0.0 : (reservedHours / totalHours) * 100.0;

                    return OccupancyReportResponse.builder()
                            .spaceId(space.getId())
                            .spaceName(space.getName())
                            .occupancyPercentage(Math.round(occupancy * 100.0) / 100.0)
                            .build();
                })
                .toList();
    }

    private double overlappingHours(LocalDateTime reservationStart,
                                    LocalDateTime reservationEnd,
                                    LocalDateTime from,
                                    LocalDateTime to) {
        LocalDateTime effectiveStart = reservationStart.isAfter(from) ? reservationStart : from;
        LocalDateTime effectiveEnd = reservationEnd.isBefore(to) ? reservationEnd : to;

        if (!effectiveStart.isBefore(effectiveEnd)) {
            return 0.0;
        }

        return Duration.between(effectiveStart, effectiveEnd).toMinutes() / 60.0;
    }
}
