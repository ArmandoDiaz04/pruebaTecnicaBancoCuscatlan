package com.pruebatecnica.bancocuscatlan.controller;

import com.pruebatecnica.bancocuscatlan.dto.CreateReservationRequest;
import com.pruebatecnica.bancocuscatlan.dto.ReservationResponse;
import com.pruebatecnica.bancocuscatlan.dto.UpdateReservationStatusRequest;
import com.pruebatecnica.bancocuscatlan.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Gestión de reservas")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Crear reserva", description = "Crea una reserva para un espacio y usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "409", description = "Conflicto por traslape de horario"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las reservas (ADMIN)")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/my")
    @Operation(summary = "Listar mis reservas")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por id")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar reservas por usuario")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar reserva", description = "USER puede cancelar sus reservas, ADMIN cualquiera")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de reserva (ADMIN)")
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationStatusRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, request.getStatus()));
    }
}
