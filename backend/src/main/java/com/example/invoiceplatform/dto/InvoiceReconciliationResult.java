package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InvoiceReconciliationResult(
        String sourceFileName,
        String invoiceNumber,
        String sellerName,
        String clientName,
        BigDecimal extractedTotal,
        BigDecimal ocrTotal,
        boolean discrepancy,
        BigDecimal difference
) {
}
