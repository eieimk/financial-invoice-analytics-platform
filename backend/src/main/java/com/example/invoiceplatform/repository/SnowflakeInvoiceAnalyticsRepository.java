package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.DashboardMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Queries the star schema in snowflake/schema/create_tables.sql. See
 * snowflake/sample-query/spend_by_seller.sql for the same shape used
 * interactively for the AP dashboard.
 */
@Slf4j
@RequiredArgsConstructor
public class SnowflakeInvoiceAnalyticsRepository implements InvoiceAnalyticsRepository {

    private static final String TOTALS_SQL = """
            SELECT COUNT(*) AS total_invoices,
                   SUM(total) AS total_revenue,
                   AVG(total) AS average_invoice_amount
            FROM fact_invoice
            """;

    private static final String TOP_VENDOR_SQL = """
            SELECT s.seller_name
            FROM fact_invoice f
            JOIN dim_seller s ON s.seller_id = f.seller_id
            GROUP BY s.seller_name
            ORDER BY SUM(f.total) DESC
            LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public DashboardMetrics fetchDashboardMetrics() {
        Map<String, Object> totals = jdbcTemplate.queryForMap(TOTALS_SQL, Collections.emptyMap());
        String topVendor = jdbcTemplate.query(TOP_VENDOR_SQL, Collections.emptyMap(),
                        rs -> rs.next() ? rs.getString("seller_name") : null);

        DashboardMetrics metrics = DashboardMetrics.builder()
                .totalInvoices(((Number) totals.get("TOTAL_INVOICES")).longValue())
                .totalRevenue(asBigDecimal(totals.get("TOTAL_REVENUE")))
                .averageInvoiceAmount(asBigDecimal(totals.get("AVERAGE_INVOICE_AMOUNT")))
                .topVendor(topVendor)
                .build();

        log.debug("Fetched dashboard metrics from Snowflake: {}", metrics);
        return metrics;
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal bigDecimal ? bigDecimal : new BigDecimal(value.toString());
    }
}
