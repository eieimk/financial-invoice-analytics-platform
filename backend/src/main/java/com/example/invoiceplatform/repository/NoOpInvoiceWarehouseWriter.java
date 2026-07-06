package com.example.invoiceplatform.repository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Used when no Snowflake credentials are configured: uploads still land in S3
 * and parse/reconcile locally, they just don't reach a warehouse.
 */
@Slf4j
public class NoOpInvoiceWarehouseWriter implements InvoiceWarehouseWriter {

    @Override
    public int insertRawInvoices(List<String> jsonRows) {
        log.debug("Snowflake not configured; skipping warehouse insert of {} rows", jsonRows.size());
        return 0;
    }

    @Override
    public boolean triggerLoadTasks() {
        return false;
    }
}
