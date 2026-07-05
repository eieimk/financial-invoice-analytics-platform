package com.example.invoiceplatform.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AgingBucket(
        String bucket,   // NO_DUE_DATE | CURRENT | 1-30_DAYS | 31-60_DAYS | 60_PLUS_DAYS
        long invoiceCount,
        BigDecimal totalAmount
) {
}
