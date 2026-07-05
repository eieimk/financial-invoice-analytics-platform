-- JSON-internal reconciliation: the extraction's line items should sum to its
-- own stated total (SUM(items[].total_price) = subtotal.total). A gap means
-- the OCR extraction dropped or misread a line item or the total - flag it
-- for manual review before the invoice is trusted for AP analytics.
-- (The OCR-text-vs-JSON cross-check runs Java-side in
-- InvoiceReconciliationService before the file ever lands here.)

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE TASK IF NOT EXISTS reconciliation_exception_task
    WAREHOUSE = INVOICE_ANALYTICS_WH
    SCHEDULE = '5 MINUTE'
    WHEN SYSTEM$STREAM_HAS_DATA('reconciliation_stream')
AS
INSERT INTO fact_reconciliation_exception
    (raw_invoice_id, invoice_number, line_item_sum, stated_total, difference, is_discrepancy)
SELECT
    id,
    payload:invoice:invoice_number::STRING AS invoice_number,
    line_item_sum,
    stated_total,
    ABS(line_item_sum - stated_total) AS difference,
    (line_item_sum IS NULL OR stated_total IS NULL
        OR ABS(line_item_sum - stated_total) > 0.01) AS is_discrepancy
FROM (
    -- LEFT OUTER lateral join so an extraction with a missing/empty items[]
    -- still produces a row (with NULL line_item_sum -> flagged as discrepancy)
    SELECT
        r.id,
        PARSE_JSON(r.json_data) AS payload,
        TRY_TO_DECIMAL(PARSE_JSON(r.json_data):subtotal:total::STRING, 18, 2) AS stated_total,
        SUM(TRY_TO_DECIMAL(item.value:total_price::STRING, 18, 2)) AS line_item_sum
    FROM reconciliation_stream r,
         LATERAL FLATTEN(input => PARSE_JSON(r.json_data):items, OUTER => TRUE) item
    GROUP BY r.id, r.json_data
) parsed;

-- New tasks are created SUSPENDED by default.
-- ALTER TASK reconciliation_exception_task RESUME;
