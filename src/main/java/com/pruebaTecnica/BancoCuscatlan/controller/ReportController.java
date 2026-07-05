package com.pruebaTecnica.BancoCuscatlan.controller;

import com.pruebaTecnica.BancoCuscatlan.dto.ReservationReportResponse;
import com.pruebaTecnica.BancoCuscatlan.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
