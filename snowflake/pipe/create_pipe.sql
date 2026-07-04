-- Auto-ingest pipe: fires whenever the backend's S3FileStorageService lands a
-- new object under s3://invoices-buckets/invoices/ (see S3FileStorageService.store).
-- AUTO_INGEST relies on an S3 event notification -> SQS queue that AWS pushes
-- to on ObjectCreated; Snowflake owns polling that queue, not the backend.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

CREATE PIPE IF NOT EXISTS invoice_ocr_pipe
    AUTO_INGEST = TRUE
AS
    COPY INTO raw_invoice_ocr (file_name, json_data, ocr_text, _stage_file)
    FROM (
        SELECT $1, $2, $3, METADATA$FILENAME
        FROM @invoice_ocr_stage
    )
    FILE_FORMAT = (FORMAT_NAME = csv_invoice_ocr_format)
    ON_ERROR = 'CONTINUE';

-- After creating the pipe, copy SYSTEM$PIPE_STATUS('invoice_ocr_pipe') ->
-- notification_channel ARN into the S3 bucket's event notification config
-- (this is the one piece of wiring that lives outside SQL, in the AWS console
-- or Terraform - a one-time setup step, not a per-file operation).

-- Manual backfill / local dev (no event notifications configured yet):
-- ALTER PIPE invoice_ocr_pipe REFRESH;
