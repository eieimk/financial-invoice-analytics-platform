-- Extraction-quality report: invoices whose JSON line items don't sum to the
-- JSON's own stated total, flagged for manual review.
USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

SELECT
    raw_invoice_id,
    invoice_number,
    line_item_sum,
    stated_total,
    difference,
    checked_at
FROM fact_reconciliation_exception
WHERE is_discrepancy = TRUE
ORDER BY difference DESC NULLS FIRST;
