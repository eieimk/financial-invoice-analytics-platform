package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.AgingBucket;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.model.MonthlySpend;
import com.example.invoiceplatform.model.ProductLine;
import com.example.invoiceplatform.model.SellerSpend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Queries the star schema in snowflake/schema/create_tables.sql. See
 * snowflake/sample-query/spend_by_seller.sql for the same shape used
 * interactively for the AP dashboard.
 */
@Slf4j
public record SnowflakeInvoiceAnalyticsRepository(
        NamedParameterJdbcTemplate jdbcTemplate) implements InvoiceAnalyticsRepository {

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
            ORDER BY SUM(f.total) DESC NULLS LAST
            LIMIT 1
            """;

    private static final String SPEND_BY_SELLER_SQL = """
            SELECT s.seller_name,
                   COUNT(*) AS invoice_count,
                   SUM(f.total) AS total_spend,
                   ROUND(AVG(f.total), 2) AS avg_invoice_amount
            FROM fact_invoice f
            JOIN dim_seller s ON s.seller_id = f.seller_id
            GROUP BY s.seller_name
            ORDER BY total_spend DESC NULLS LAST
            LIMIT :limit
            """;

    // Reads the star schema directly rather than the daily_summary dynamic
    // table so the endpoint works even before the dynamic tables' first
    // TARGET_LAG refresh; daily_summary stays the interactive/BI read path.
    private static final String MONTHLY_TREND_SQL = """
            SELECT TO_CHAR(d.date, 'YYYY-MM') AS month,
                   SUM(f.total) AS total_spend,
                   COUNT(*) AS invoice_count
            FROM fact_invoice f
            JOIN dim_date d ON d.date_id = f.invoice_date_id
            GROUP BY TO_CHAR(d.date, 'YYYY-MM')
            ORDER BY month
            """;

    private static final String AGING_SQL = """
            SELECT CASE
                       WHEN f.due_date IS NULL THEN 'NO_DUE_DATE'
                       WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 0 THEN 'CURRENT'
                       WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 30 THEN '1-30_DAYS'
                       WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 60 THEN '31-60_DAYS'
                       ELSE '60_PLUS_DAYS'
                   END AS aging_bucket,
                   COUNT(*) AS invoice_count,
                   SUM(f.total) AS total_amount
            FROM fact_invoice f
            GROUP BY aging_bucket
            """;

    private static final String PRODUCT_LINES_SQL = """
            SELECT l.description,
                   l.quantity,
                   l.total_price,
                   d.date AS invoice_date,
                   s.seller_name
            FROM fact_invoice_line l
            JOIN fact_invoice f ON f.invoice_number = l.invoice_number
            LEFT JOIN dim_date d ON d.date_id = f.invoice_date_id
            LEFT JOIN dim_seller s ON s.seller_id = f.seller_id
            ORDER BY d.date NULLS LAST
            LIMIT :limit
            """;

    @Override
    public DashboardMetrics fetchDashboardMetrics() {
        Map<String, Object> totals = jdbcTemplate.queryForMap(TOTALS_SQL, Collections.emptyMap());
        String topVendor = jdbcTemplate.query(TOP_VENDOR_SQL, Collections.emptyMap(),
                        rs -> rs.next() ? rs.getString(1) : null);

        DashboardMetrics metrics = DashboardMetrics.builder()
                .totalInvoices(((Number) totals.get("TOTAL_INVOICES")).longValue())
                .totalRevenue(asBigDecimal(totals.get("TOTAL_REVENUE")))
                .averageInvoiceAmount(asBigDecimal(totals.get("AVERAGE_INVOICE_AMOUNT")))
                .topVendor(topVendor)
                .build();

        log.debug("Fetched dashboard metrics from Snowflake: {}", metrics);
        return metrics;
    }

    @Override
    public List<SellerSpend> fetchSpendBySeller(int limit) {
        // Columns fetched by position: Snowflake folds unquoted identifiers to
        // uppercase and its driver's label lookup is case-sensitive.
        return jdbcTemplate.query(SPEND_BY_SELLER_SQL, Map.of("limit", limit), (rs, rowNum) ->
                SellerSpend.builder()
                        .sellerName(rs.getString(1))
                        .invoiceCount(rs.getLong(2))
                        .totalSpend(asBigDecimal(rs.getBigDecimal(3)))
                        .avgInvoiceAmount(asBigDecimal(rs.getBigDecimal(4)))
                        .build());
    }

    @Override
    public List<MonthlySpend> fetchMonthlyTrend() {
        return jdbcTemplate.query(MONTHLY_TREND_SQL, Collections.emptyMap(), (rs, rowNum) ->
                MonthlySpend.builder()
                        .month(rs.getString(1))
                        .totalSpend(asBigDecimal(rs.getBigDecimal(2)))
                        .invoiceCount(rs.getLong(3))
                        .build());
    }

    @Override
    public List<AgingBucket> fetchInvoiceAging() {
        return jdbcTemplate.query(AGING_SQL, Collections.emptyMap(), (rs, rowNum) ->
                AgingBucket.builder()
                        .bucket(rs.getString(1))
                        .invoiceCount(rs.getLong(2))
                        .totalAmount(asBigDecimal(rs.getBigDecimal(3)))
                        .build());
    }

    @Override
    public List<ProductLine> fetchProductLines(int limit) {
        return jdbcTemplate.query(PRODUCT_LINES_SQL, Map.of("limit", limit), (rs, rowNum) ->
                ProductLine.builder()
                        .description(rs.getString(1))
                        .quantity(asBigDecimal(rs.getBigDecimal(2)))
                        .totalPrice(asBigDecimal(rs.getBigDecimal(3)))
                        .invoiceDate(rs.getDate(4) == null ? null : rs.getDate(4).toLocalDate())
                        .sellerName(rs.getString(5))
                        .build());
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value instanceof BigDecimal bigDecimal ? bigDecimal : new BigDecimal(value.toString());
    }
}
