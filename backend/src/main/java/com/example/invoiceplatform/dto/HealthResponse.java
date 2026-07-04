package com.example.invoiceplatform.dto;

public record HealthResponse(String status) {

    public static HealthResponse up() {
        return new HealthResponse("UP");
    }
}
