package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "Service liveness")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Operation(summary = "Liveness probe")
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.up());
    }
}
