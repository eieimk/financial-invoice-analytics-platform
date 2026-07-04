-- Captures newly landed rows on the raw staging table so the tasks below
-- only process what's new since their last run, instead of rescanning
-- raw_invoice_ocr in full every time.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE STREAM IF NOT EXISTS raw_invoice_ocr_stream
    ON TABLE raw_invoice_ocr
    APPEND_ONLY = TRUE;   -- landing rows are never updated/deleted in place
