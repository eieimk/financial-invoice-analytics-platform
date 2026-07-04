package com.example.invoiceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentInstructions(
        @JsonProperty("due_date") String dueDate,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("account_number") String accountNumber,
        @JsonProperty("payment_method") String paymentMethod
) {
}
