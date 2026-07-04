package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.DashboardMetrics;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Fallback used when no Snowflake credentials are configured (see
 * InvoiceAnalyticsRepositoryConfig). Values mirror the shape the star-schema
 * aggregation query returns.
 */
@Slf4j
public class MockInvoiceAnalyticsRepository implements InvoiceAnalyticsRepository {

    @Override
    public DashboardMetrics fetchDashboardMetrics() {
        log.debug("Serving mock dashboard metrics (Snowflake integration pending)");
        return DashboardMetrics.builder()
                .totalInvoices(1_248L)
                .totalRevenue(new BigDecimal("4823650.75"))
                .averageInvoiceAmount(new BigDecimal("3865.10"))
                .topVendor("Patel, Thompson and Montgomery")
                .build();
    }
}
