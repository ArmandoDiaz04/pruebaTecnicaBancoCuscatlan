package com.pruebatecnica.bancocuscatlan.controller;

import com.pruebatecnica.bancocuscatlan.dto.CreateSpaceRequest;
import com.pruebatecnica.bancocuscatlan.dto.SpaceResponse;
import com.pruebatecnica.bancocuscatlan.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar espacio", description = "Actualiza los datos de un espacio")
    public ResponseEntity<SpaceResponse> updateSpace(@PathVariable Long id, @Valid @RequestBody CreateSpaceRequest request) {
        return ResponseEntity.ok(spaceService.updateSpace(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar espacio", description = "Desactiva un espacio por su id (soft delete)")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar espacios inactivos", description = "ADMIN: ver espacios desactivados")
    public ResponseEntity<List<SpaceResponse>> getInactiveSpaces() {
        return ResponseEntity.ok(spaceService.getInactiveSpaces());
    }
}
