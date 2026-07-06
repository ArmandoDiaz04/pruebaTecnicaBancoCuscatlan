package com.pruebatecnica.bancocuscatlan.controller;

import com.pruebatecnica.bancocuscatlan.dto.OccupancyReportResponse;
import com.pruebatecnica.bancocuscatlan.dto.ReservationReportResponse;
import com.pruebatecnica.bancocuscatlan.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Reportes administrativos")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reservations")
    @Operation(summary = "Reporte de reservas", description = "Resumen de reservas por estado")
    public ResponseEntity<ReservationReportResponse> reservationSummary() {
        return ResponseEntity.ok(reportService.getReservationSummary());
    }

    @GetMapping("/occupancy")
    @Operation(summary = "Reporte de ocupación por espacio")
    public ResponseEntity<List<OccupancyReportResponse>> occupancyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getOccupancyReport(from, to));
    }
}
