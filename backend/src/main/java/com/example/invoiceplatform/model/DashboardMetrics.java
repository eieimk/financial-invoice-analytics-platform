package com.example.invoiceplatform.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Domain model for aggregated invoice metrics. Populated from mock data today;
 * the Snowflake-backed repository will produce the same shape once wired in.
 */
@Builder
public record DashboardMetrics(
        long totalInvoices,
        BigDecimal totalRevenue,
        BigDecimal averageInvoiceAmount,
        String topVendor
) {
}
