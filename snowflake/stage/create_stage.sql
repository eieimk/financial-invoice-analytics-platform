-- External stage over the same S3 landing zone the backend writes to
-- (see backend AwsS3Properties / application-prod.yml: bucket "invoices-buckets",
-- key prefix "invoices/"). Snowflake reads from here; it never writes to S3.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

-- A storage integration keeps the IAM role out of SQL/version control.
-- STORAGE_AWS_ROLE_ARN is created once by an admin outside this script;
-- application code never touches Snowflake's copy of the credentials.
CREATE STORAGE INTEGRATION IF NOT EXISTS invoice_s3_integration
    TYPE = EXTERNAL_STAGE
    STORAGE_PROVIDER = 'S3'
    ENABLED = TRUE
    STORAGE_AWS_ROLE_ARN = '<arn:aws:iam::ACCOUNT_ID:role/snowflake-invoice-reader>'
    STORAGE_ALLOWED_LOCATIONS = ('s3://invoices-buckets/invoices/');

-- The Json Data / OCRed Text columns embed literal newlines inside quoted
-- fields (multi-line OCR output); Snowflake's CSV reader honors the
-- enclosing quotes by default so a record is delimited by the closing
-- quote, not the first newline - no special option needed for that.
CREATE FILE FORMAT IF NOT EXISTS csv_invoice_ocr_format
    TYPE = CSV
    FIELD_OPTIONALLY_ENCLOSED_BY = '"'
    SKIP_HEADER = 1;

CREATE STAGE IF NOT EXISTS invoice_ocr_stage
    URL = 's3://invoices-buckets/invoices/'
    STORAGE_INTEGRATION = invoice_s3_integration
    FILE_FORMAT = csv_invoice_ocr_format;

-- Sanity check after creation: SELECT * FROM DIRECTORY(@invoice_ocr_stage);
