package com.example.invoiceplatform.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MonthlySpend(
        String month,   // ISO yyyy-MM, e.g. "2021-03"
        BigDecimal totalSpend,
        long invoiceCount
) {
}
