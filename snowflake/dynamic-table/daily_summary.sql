-- "Gold" layer built on top of item_detail: one row per (day, seller), the
-- shape the dashboard's spend-by-vendor chart queries directly. Chained
-- dynamic tables mean this refreshes automatically after item_detail does,
-- Snowflake sequences the DAG - no manual ordering of jobs.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE OR REPLACE DYNAMIC TABLE daily_summary
    TARGET_LAG = '15 minutes'
    WAREHOUSE = INVOICE_ANALYTICS_WH
AS
WITH invoice_level AS (
    -- collapse back to one row per invoice first - item_detail is at line
    -- grain, so invoice_total repeats once per line and can't be summed directly
    SELECT DISTINCT
        invoice_number,
        invoice_date,
        seller_name,
        invoice_total
    FROM item_detail
)
SELECT
    d.invoice_date,
    d.seller_name,
    COUNT(DISTINCT d.invoice_number) AS invoice_count,
    SUM(l.total_price)               AS line_item_total,
    SUM(d.invoice_total)             AS invoice_total_sum,
    AVG(d.invoice_total)             AS avg_invoice_amount
FROM invoice_level d
JOIN item_detail l ON l.invoice_number = d.invoice_number
GROUP BY d.invoice_date, d.seller_name;
