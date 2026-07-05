-- AP spend by vendor - the headline chart on the dashboard.
USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

SELECT
    s.seller_name,
    COUNT(*)             AS invoice_count,
    SUM(f.total)          AS total_spend,
    AVG(f.total)          AS avg_invoice_amount
FROM fact_invoice f
JOIN dim_seller s ON s.seller_id = f.seller_id
GROUP BY s.seller_name
ORDER BY total_spend DESC;
