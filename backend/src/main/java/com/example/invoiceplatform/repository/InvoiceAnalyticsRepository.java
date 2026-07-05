package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.DashboardMetrics;

/**
 * Data-access boundary for invoice analytics. The service layer depends only on
 * this interface, so swapping the mock implementation for a Snowflake-backed one
 * (JdbcTemplate over the star schema) requires no changes above this layer.
 */
public interface InvoiceAnalyticsRepository {

    DashboardMetrics fetchDashboardMetrics();
}
