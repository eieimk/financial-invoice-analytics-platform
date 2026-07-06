package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MonthlySpendResponse(
        String month,
        BigDecimal totalSpend,
        long invoiceCount
) {
}
