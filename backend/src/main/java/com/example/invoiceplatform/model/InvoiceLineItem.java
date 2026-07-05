package com.example.invoiceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceLineItem(
        String description,
        String quantity,
        @JsonProperty("total_price") String totalPrice
) {
}
