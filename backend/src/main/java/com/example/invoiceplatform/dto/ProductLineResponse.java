package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ProductLineResponse(
        String description,
        BigDecimal quantity,
        BigDecimal totalPrice,
        LocalDate invoiceDate,
        String sellerName
) {
}
