-- "Silver" layer: flattens the raw JSON extraction into invoice-line grain.
-- Declarative alternative to a hand-written stream+task transform - Snowflake
-- works out the refresh DAG and re-runs only what changed upstream.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE OR REPLACE DYNAMIC TABLE item_detail
    TARGET_LAG = '5 minutes'
    WAREHOUSE = INVOICE_ANALYTICS_WH
AS
SELECT
    r.id                                                   AS raw_invoice_id,
    j.value:invoice:invoice_number::STRING                 AS invoice_number,
    TRY_TO_DATE(j.value:invoice:invoice_date::STRING, 'MM/DD/YYYY')  AS invoice_date,
    TRY_TO_DATE(j.value:invoice:due_date::STRING, 'MM/DD/YYYY')      AS due_date,
    j.value:invoice:seller_name::STRING                    AS seller_name,
    j.value:invoice:seller_address::STRING                 AS seller_address,
    j.value:invoice:client_name::STRING                    AS client_name,
    j.value:invoice:client_address::STRING                 AS client_address,
    item.index + 1                                         AS line_number,
    item.value:description::STRING                         AS description,
    TRY_TO_DECIMAL(item.value:quantity::STRING, 18, 2)      AS quantity,
    TRY_TO_DECIMAL(item.value:total_price::STRING, 18, 2)   AS total_price,
    TRY_TO_DECIMAL(j.value:subtotal:tax::STRING, 18, 2)      AS tax,
    TRY_TO_DECIMAL(j.value:subtotal:discount::STRING, 18, 2) AS discount,
    TRY_TO_DECIMAL(j.value:subtotal:total::STRING, 18, 2)    AS invoice_total
FROM raw_invoice_ocr r,
     LATERAL (SELECT PARSE_JSON(r.json_data) AS value) j,
     LATERAL FLATTEN(input => j.value:items) item;
