package com.pruebaTecnica.BancoCuscatlan.controller;

import com.pruebaTecnica.BancoCuscatlan.domain.enums.ReservationStatus;
import com.pruebaTecnica.BancoCuscatlan.dto.ReservationResponse;
import com.pruebaTecnica.BancoCuscatlan.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@Tag(name = "Admin Reservations", description = "Gestión administrativa de reservas")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las reservas")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cualquier reserva (ADMIN)")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de reserva (ADMIN)")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status
    ) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, status));
    }
}