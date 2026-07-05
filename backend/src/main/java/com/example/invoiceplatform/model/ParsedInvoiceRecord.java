package com.example.invoiceplatform.model;

/**
 * One parsed row of the upload CSV. The source format is a single JSON_DATA
 * column, so the row number is the only provenance available for error
 * messages and reconciliation reports. rawJson is the column text verbatim —
 * the warehouse landing zone stores that, not a re-serialization, so unknown
 * fields the DTOs ignore aren't silently dropped.
 */
public record ParsedInvoiceRecord(
        long rowNumber,
        String rawJson,
        InvoiceJsonPayload extraction
) {
}
