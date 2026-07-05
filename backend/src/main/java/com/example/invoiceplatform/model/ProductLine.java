package com.example.invoiceplatform.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ProductLine(
        String description,
        BigDecimal quantity,
        BigDecimal totalPrice,
        LocalDate invoiceDate,
        String sellerName
) {
}
