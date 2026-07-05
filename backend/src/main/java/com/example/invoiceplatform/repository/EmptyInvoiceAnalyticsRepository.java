package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.AgingBucket;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.model.MonthlySpend;
import com.example.invoiceplatform.model.ProductLine;
import com.example.invoiceplatform.model.SellerSpend;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * Used when no Snowflake credentials are configured (see
 * InvoiceAnalyticsRepositoryConfig). Returns zeros/empty lists rather than
 * fabricated numbers, so nothing on the dashboard can be mistaken for real
 * data - local dev without credentials still boots, it just shows nothing.
 */
@Slf4j
public class EmptyInvoiceAnalyticsRepository implements InvoiceAnalyticsRepository {

    @Override
    public DashboardMetrics fetchDashboardMetrics() {
        log.debug("Snowflake not configured; returning empty dashboard metrics");
        return DashboardMetrics.builder()
                .totalInvoices(0L)
                .totalRevenue(BigDecimal.ZERO)
                .averageInvoiceAmount(BigDecimal.ZERO)
                .topVendor(null)
                .build();
    }

    @Override
    public List<SellerSpend> fetchSpendBySeller(int limit) {
        return List.of();
    }

    @Override
    public List<MonthlySpend> fetchMonthlyTrend() {
        return List.of();
    }

    @Override
    public List<AgingBucket> fetchInvoiceAging() {
        return List.of();
    }

    @Override
    public List<ProductLine> fetchProductLines(int limit) {
        return List.of();
    }
}
