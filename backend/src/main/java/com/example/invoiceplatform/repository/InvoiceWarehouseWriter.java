package com.example.invoiceplatform.repository;

import java.util.List;

/**
 * Write path into the warehouse landing zone. In production the S3 -> Snowpipe
 * auto-ingest would own this; writing directly over JDBC lets the demo close
 * the loop (upload -> dashboard) without the AWS event-notification setup.
 */
public interface InvoiceWarehouseWriter {

    /** Lands raw JSON rows in raw_invoice_ocr. Returns rows written. */
    int insertRawInvoices(List<String> jsonRows);

    /**
     * Kicks the star-schema + reconciliation tasks so freshly landed rows are
     * queryable in seconds instead of on the 5-minute schedule. Returns false
     * (without throwing) if the tasks couldn't be triggered — they'll still
     * run on schedule.
     */
    boolean triggerLoadTasks();
}
