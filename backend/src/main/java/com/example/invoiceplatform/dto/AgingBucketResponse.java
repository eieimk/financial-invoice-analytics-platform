package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AgingBucketResponse(
        String bucket,
        long invoiceCount,
        BigDecimal totalAmount
) {
}
