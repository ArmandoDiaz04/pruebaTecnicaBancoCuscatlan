package com.pruebaTecnica.BancoCuscatlan.controller;

import com.pruebaTecnica.BancoCuscatlan.dto.CreateSpaceRequest;
import com.pruebaTecnica.BancoCuscatlan.dto.SpaceResponse;
import com.pruebaTecnica.BancoCuscatlan.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "Spaces", description = "Gestión de espacios de coworking")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping
    @Operation(summary = "Crear espacio", description = "Crea un nuevo espacio de coworking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Espacio creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<SpaceResponse> createSpace(@Valid @RequestBody CreateSpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.createSpace(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener espacio por id")
    public ResponseEntity<SpaceResponse> getSpaceById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getSpaceById(id));
    }

    @GetMapping
    @Operation(summary = "Listar espacios")
    public ResponseEntity<List<SpaceResponse>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.getAllSpaces());
    }
}
