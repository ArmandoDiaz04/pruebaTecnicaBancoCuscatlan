package com.pruebatecnica.bancocuscatlan.controller;

import com.pruebatecnica.bancocuscatlan.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoints para verificar el estado de la API")
public class HealthController {

    @Operation(
        summary = "Verifica el estado de la API",
        description = "Endpoint para comprobar que la API está funcionando correctamente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API funcionando correctamente")
    })
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .message("Banco Cuscatlán API está funcionando correctamente")
                .timestamp(LocalDateTime.now())
                .version("1.0.0")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
