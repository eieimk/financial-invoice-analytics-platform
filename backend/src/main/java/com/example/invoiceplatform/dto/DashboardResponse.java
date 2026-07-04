package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DashboardResponse(
        long totalInvoices,
        BigDecimal totalRevenue,
        BigDecimal averageInvoiceAmount,
        String topVendor
) {
}
