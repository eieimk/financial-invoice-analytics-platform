package com.example.invoiceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceHeader(
        @JsonProperty("client_name") String clientName,
        @JsonProperty("client_address") String clientAddress,
        @JsonProperty("seller_name") String sellerName,
        @JsonProperty("seller_address") String sellerAddress,
        @JsonProperty("invoice_number") String invoiceNumber,
        @JsonProperty("invoice_date") String invoiceDate,
        @JsonProperty("due_date") String dueDate
) {
}
