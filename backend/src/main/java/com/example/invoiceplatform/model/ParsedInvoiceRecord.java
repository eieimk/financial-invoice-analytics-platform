package com.example.invoiceplatform.model;

public record ParsedInvoiceRecord(
        String sourceFileName,
        InvoiceJsonPayload extraction,
        String ocrText
) {
}
