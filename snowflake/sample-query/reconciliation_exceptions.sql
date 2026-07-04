-- Extraction-quality report: OCR rows where the JSON extraction and the raw
-- OCR text disagree on the invoice total, flagged for manual review.
USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

SELECT
    source_file_name,
    invoice_number,
    extracted_total,
    ocr_total,
    difference,
    checked_at
FROM fact_reconciliation_exception
WHERE is_discrepancy = TRUE
ORDER BY difference DESC NULLS FIRST;
