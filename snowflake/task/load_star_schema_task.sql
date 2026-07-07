-- Loads new raw rows into the dimensional model. This is the part of the
-- pipeline that's genuinely procedural (surrogate-key lookups + upserts)
-- rather than a pure transform, so it's a task+stream, not a dynamic table.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

-- CREATE OR REPLACE (not IF NOT EXISTS) so re-running this script applies
-- fixes to the existing task. Replacing leaves it SUSPENDED - run the
-- ALTER ... RESUME at the bottom afterwards.
CREATE OR REPLACE TASK load_star_schema_task
    WAREHOUSE = INVOICE_ANALYTICS_WH
    SCHEDULE = '5 MINUTE'
    WHEN SYSTEM$STREAM_HAS_DATA('raw_invoice_ocr_stream')
AS
BEGIN
    -- A stream's offset advances as soon as any statement reads it, so the
    -- stream is read exactly once here and materialized into a temp table;
    -- every MERGE below reads that snapshot instead of re-querying the stream.
    CREATE OR REPLACE TEMPORARY TABLE _batch AS
        SELECT
            id AS raw_invoice_id,
            PARSE_JSON(json_data) AS payload
        FROM raw_invoice_ocr_stream;

    -- dim_seller
    MERGE INTO dim_seller t
    USING (
        SELECT DISTINCT
            SHA1_HEX(payload:invoice:seller_name::STRING || payload:invoice:seller_address::STRING) AS seller_id,
            payload:invoice:seller_name::STRING    AS seller_name,
            payload:invoice:seller_address::STRING AS seller_address
        FROM _batch
    ) s
    ON t.seller_id = s.seller_id
    WHEN NOT MATCHED THEN
        INSERT (seller_id, seller_name, seller_address)
        VALUES (s.seller_id, s.seller_name, s.seller_address);

    -- dim_client
    MERGE INTO dim_client t
    USING (
        SELECT DISTINCT
            SHA1_HEX(payload:invoice:client_name::STRING || payload:invoice:client_address::STRING) AS client_id,
            payload:invoice:client_name::STRING    AS client_name,
            payload:invoice:client_address::STRING AS client_address
        FROM _batch
    ) c
    ON t.client_id = c.client_id
    WHEN NOT MATCHED THEN
        INSERT (client_id, client_name, client_address)
        VALUES (c.client_id, c.client_name, c.client_address);

    -- dim_date (invoice_date only; due_date stored raw on fact_invoice since
    -- it's often blank in the source and doesn't need its own dimension row)
    MERGE INTO dim_date t
    USING (
        SELECT DISTINCT
            TO_NUMBER(TO_CHAR(TRY_TO_DATE(payload:invoice:invoice_date::STRING, 'MM/DD/YYYY'), 'YYYYMMDD')) AS date_id,
            TRY_TO_DATE(payload:invoice:invoice_date::STRING, 'MM/DD/YYYY') AS date
        FROM _batch
        WHERE TRY_TO_DATE(payload:invoice:invoice_date::STRING, 'MM/DD/YYYY') IS NOT NULL
    ) d
    ON t.date_id = d.date_id
    WHEN NOT MATCHED THEN
        INSERT (date_id, date, day, month, quarter, year, dow_name)
        VALUES (d.date_id, d.date, DAY(d.date), MONTH(d.date), QUARTER(d.date), YEAR(d.date), DAYNAME(d.date));

    -- fact_invoice
    MERGE INTO fact_invoice t
    USING (
        SELECT
            payload:invoice:invoice_number::STRING AS invoice_number,
            raw_invoice_id,
            SHA1_HEX(payload:invoice:seller_name::STRING || payload:invoice:seller_address::STRING) AS seller_id,
            SHA1_HEX(payload:invoice:client_name::STRING || payload:invoice:client_address::STRING) AS client_id,
            TO_NUMBER(TO_CHAR(TRY_TO_DATE(payload:invoice:invoice_date::STRING, 'MM/DD/YYYY'), 'YYYYMMDD')) AS invoice_date_id,
            TRY_TO_DATE(payload:invoice:due_date::STRING, 'MM/DD/YYYY') AS due_date,
            TRY_TO_DECIMAL(payload:subtotal:tax::STRING, 18, 2)      AS tax,
            TRY_TO_DECIMAL(payload:subtotal:discount::STRING, 18, 2) AS discount,
            TRY_TO_DECIMAL(payload:subtotal:total::STRING, 18, 2)    AS total,
            payload:payment_instructions:bank_name::STRING      AS bank_name,
            payload:payment_instructions:account_number::STRING AS account_number,
            payload:payment_instructions:payment_method::STRING AS payment_method
        FROM _batch
    ) s
    ON t.invoice_number = s.invoice_number
    WHEN NOT MATCHED THEN
        INSERT (invoice_number, raw_invoice_id, seller_id, client_id, invoice_date_id,
                due_date, tax, discount, total, bank_name, account_number, payment_method)
        VALUES (s.invoice_number, s.raw_invoice_id, s.seller_id, s.client_id, s.invoice_date_id,
                s.due_date, s.tax, s.discount, s.total, s.bank_name, s.account_number, s.payment_method);

    -- fact_invoice_line: delete-then-insert keyed on the batch's invoice
    -- numbers, so re-landing the same invoice (task retry, repeated upload of
    -- the same file) replaces its lines instead of duplicating them.
    DELETE FROM fact_invoice_line
    WHERE invoice_number IN (
        SELECT DISTINCT payload:invoice:invoice_number::STRING FROM _batch
    );

    INSERT INTO fact_invoice_line (invoice_number, line_number, description, quantity, total_price)
    SELECT
        payload:invoice:invoice_number::STRING AS invoice_number,
        item.index + 1                         AS line_number,
        item.value:description::STRING         AS description,
        TRY_TO_DECIMAL(item.value:quantity::STRING, 18, 2)    AS quantity,
        TRY_TO_DECIMAL(item.value:total_price::STRING, 18, 2) AS total_price
    FROM (
        -- a re-uploaded invoice appears once per landing; keep only the
        -- newest raw row per invoice so its lines aren't inserted twice
        SELECT payload
        FROM _batch
        QUALIFY ROW_NUMBER() OVER (
            PARTITION BY payload:invoice:invoice_number::STRING
            ORDER BY raw_invoice_id DESC
        ) = 1
    ), LATERAL FLATTEN(input => payload:items) item;
END;

-- Tasks are created/replaced SUSPENDED by default.
ALTER TASK load_star_schema_task RESUME;
