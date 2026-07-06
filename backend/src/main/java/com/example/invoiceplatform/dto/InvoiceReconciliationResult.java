package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InvoiceReconciliationResult(
        long rowNumber,
        String invoiceNumber,
        String sellerName,
        String clientName,
        BigDecimal lineItemSum,
        BigDecimal statedTotal,
        boolean discrepancy,
        BigDecimal difference
) {
}
