package com.example.invoiceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceSubtotal(
        String tax,
        String discount,
        String total
) {
}
