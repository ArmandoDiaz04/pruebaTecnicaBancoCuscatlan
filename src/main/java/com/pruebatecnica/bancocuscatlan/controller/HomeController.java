package com.pruebatecnica.bancocuscatlan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "message", "Banco Cuscatlán API está funcionando correctamente",
                "health", "/api/health",
                "swagger", "/swagger-ui.html"
        ));
    }
}