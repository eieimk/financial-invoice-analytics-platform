-- Invoice aging: buckets unpaid invoices by days past due, the classic
-- AP-automation report ("how much are we late on, and to whom").
USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

SELECT
    s.seller_name,
    f.invoice_number,
    f.due_date,
    DATEDIFF('day', f.due_date, CURRENT_DATE()) AS days_past_due,
    CASE
        WHEN f.due_date IS NULL THEN 'NO_DUE_DATE'
        WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 0 THEN 'CURRENT'
        WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 30 THEN '1-30_DAYS'
        WHEN DATEDIFF('day', f.due_date, CURRENT_DATE()) <= 60 THEN '31-60_DAYS'
        ELSE '60_PLUS_DAYS'
    END AS aging_bucket,
    f.total
FROM fact_invoice f
JOIN dim_seller s ON s.seller_id = f.seller_id
ORDER BY days_past_due DESC NULLS LAST;
