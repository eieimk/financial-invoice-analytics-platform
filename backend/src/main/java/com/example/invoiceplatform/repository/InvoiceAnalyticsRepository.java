package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.AgingBucket;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.model.MonthlySpend;
import com.example.invoiceplatform.model.ProductLine;
import com.example.invoiceplatform.model.SellerSpend;

import java.util.List;

/**
 * Data-access boundary for invoice analytics. The service layer depends only on
 * this interface, so swapping the mock implementation for a Snowflake-backed one
 * (JdbcTemplate over the star schema) requires no changes above this layer.
 */
public interface InvoiceAnalyticsRepository {

    DashboardMetrics fetchDashboardMetrics();

    List<SellerSpend> fetchSpendBySeller(int limit);

    List<MonthlySpend> fetchMonthlyTrend();

    List<AgingBucket> fetchInvoiceAging();

    List<ProductLine> fetchProductLines(int limit);
}
