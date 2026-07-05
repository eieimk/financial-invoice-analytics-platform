-- Separate stream, same source table. A stream's offset advances the moment
-- any consumer reads it, so two independent tasks sharing one stream would
-- race for the same rows - each task gets its own stream instead.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE STREAM IF NOT EXISTS reconciliation_stream
    ON TABLE raw_invoice_ocr
    APPEND_ONLY = TRUE;
