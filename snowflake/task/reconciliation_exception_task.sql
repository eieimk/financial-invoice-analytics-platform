-- SQL-side mirror of backend InvoiceReconciliationService: compares the JSON
-- extraction's total (period-decimal) against a total pulled from the raw
-- OCR text (comma-decimal, from the European-format source invoices).
-- Exists so rows that were bulk-loaded straight into the stage - bypassing
-- the /api/v1/invoices/parse endpoint - still get the same discrepancy check.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE TASK IF NOT EXISTS reconciliation_exception_task
    WAREHOUSE = INVOICE_ANALYTICS_WH
    SCHEDULE = '5 MINUTE'
    WHEN SYSTEM$STREAM_HAS_DATA('reconciliation_stream')
AS
INSERT INTO fact_reconciliation_exception
    (source_file_name, invoice_number, extracted_total, ocr_total, difference, is_discrepancy)
SELECT
    file_name,
    payload:invoice:invoice_number::STRING AS invoice_number,
    extracted_total,
    ocr_total,
    ABS(extracted_total - ocr_total) AS difference,
    (extracted_total IS NULL OR ocr_total IS NULL OR ABS(extracted_total - ocr_total) > 0.01) AS is_discrepancy
FROM (
    SELECT
        file_name,
        PARSE_JSON(json_data)                                             AS payload,
        TRY_TO_DECIMAL(PARSE_JSON(json_data):subtotal:total::STRING, 18, 2) AS extracted_total,
        -- last comma-decimal money token in the OCR text is the grand total
        -- (e.g. "... $ 211,77 $ 21,18 $ 232,95" -> 232,95), same heuristic as
        -- MoneyParser.extractLastAmount on the backend
        -- strip thousands-separator periods first, then comma -> decimal
        -- point, same two-step conversion as MoneyParser.parseCommaDecimal
        TRY_TO_DECIMAL(
            REPLACE(
                REPLACE(
                    REGEXP_SUBSTR(ocr_text, '\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2}', 1,
                        REGEXP_COUNT(ocr_text, '\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2}')),
                    '.', ''),
                ',', '.'),
            18, 2
        ) AS ocr_total
    FROM reconciliation_stream
) parsed;
