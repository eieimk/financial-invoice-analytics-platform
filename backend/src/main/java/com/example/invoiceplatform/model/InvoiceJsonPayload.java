package com.example.invoiceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceJsonPayload(
        InvoiceHeader invoice,
        List<InvoiceLineItem> items,
        InvoiceSubtotal subtotal,
        @JsonProperty("payment_instructions") PaymentInstructions paymentInstructions
) {
}
