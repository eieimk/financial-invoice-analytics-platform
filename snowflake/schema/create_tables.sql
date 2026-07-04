-- Star schema for AP invoice analytics.
-- Domain: OCR-extracted vendor invoices (client pays seller). See
-- docs/architecture.md and sample-data/invoices_ocr.csv for the source shape.

CREATE WAREHOUSE IF NOT EXISTS INVOICE_ANALYTICS_WH
    WAREHOUSE_SIZE = 'XSMALL'
    AUTO_SUSPEND = 60
    AUTO_RESUME = TRUE;

CREATE DATABASE IF NOT EXISTS INVOICE_ANALYTICS;
USE DATABASE INVOICE_ANALYTICS;
CREATE SCHEMA IF NOT EXISTS CORE;
USE SCHEMA CORE;
USE WAREHOUSE INVOICE_ANALYTICS_WH;

-- ============================================================
-- Raw landing zone: one row per source CSV row, columns kept as
-- STRING so a malformed OCR extraction never fails the COPY INTO.
-- Json Data is parsed to VARIANT downstream, not here.
-- ============================================================
CREATE TABLE IF NOT EXISTS raw_invoice_ocr (
    file_name   STRING,
    json_data   STRING,
    ocr_text    STRING,
    _stage_file STRING,   -- METADATA$FILENAME, for traceability back to S3
    loaded_at   TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP()
);

-- ============================================================
-- Dimensions
-- ============================================================
-- Seller/client have no natural id in the source (OCR gives names/addresses
-- only) so we surrogate-key on a hash of the identifying fields.
CREATE TABLE IF NOT EXISTS dim_seller (
    seller_id      STRING PRIMARY KEY,   -- SHA1_HEX(seller_name || seller_address)
    seller_name    STRING,
    seller_address STRING
);

CREATE TABLE IF NOT EXISTS dim_client (
    client_id      STRING PRIMARY KEY,   -- SHA1_HEX(client_name || client_address)
    client_name    STRING,
    client_address STRING
);

CREATE TABLE IF NOT EXISTS dim_date (
    date_id  NUMBER PRIMARY KEY,          -- YYYYMMDD
    date     DATE,
    day      NUMBER,
    month    NUMBER,
    quarter  NUMBER,
    year     NUMBER,
    dow_name STRING
);

-- ============================================================
-- Facts
-- ============================================================
CREATE TABLE IF NOT EXISTS fact_invoice (
    invoice_number   STRING PRIMARY KEY,
    source_file_name STRING,
    seller_id        STRING REFERENCES dim_seller(seller_id),
    client_id        STRING REFERENCES dim_client(client_id),
    invoice_date_id  NUMBER REFERENCES dim_date(date_id),
    due_date         DATE,
    tax              NUMBER(18,2),
    discount         NUMBER(18,2),
    total            NUMBER(18,2),
    bank_name        STRING,
    account_number   STRING,
    payment_method   STRING,
    loaded_at        TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS fact_invoice_line (
    invoice_number STRING REFERENCES fact_invoice(invoice_number),
    line_number    NUMBER,
    description    STRING,
    quantity       NUMBER(18,2),
    total_price    NUMBER(18,2),
    PRIMARY KEY (invoice_number, line_number)
);

-- Mirrors backend InvoiceReconciliationResult (see
-- backend/.../dto/InvoiceReconciliationResult.java) so the same discrepancy
-- rule that runs synchronously in the API also runs here for anything that
-- was bulk-loaded straight into the stage, bypassing the API.
CREATE TABLE IF NOT EXISTS fact_reconciliation_exception (
    source_file_name STRING,
    invoice_number   STRING,
    extracted_total  NUMBER(18,2),
    ocr_total        NUMBER(18,2),
    difference       NUMBER(18,2),
    is_discrepancy   BOOLEAN,
    checked_at       TIMESTAMP_NTZ DEFAULT CURRENT_TIMESTAMP()
);
