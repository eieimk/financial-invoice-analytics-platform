-- Monthly spend trend, reading straight off the "gold" dynamic table so the
-- dashboard doesn't need to re-aggregate item_detail on every request.
USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

SELECT
    DATE_TRUNC('month', invoice_date) AS month,
    SUM(invoice_total_sum)            AS total_spend,
    SUM(invoice_count)                AS invoice_count
FROM daily_summary
GROUP BY DATE_TRUNC('month', invoice_date)
ORDER BY month;
