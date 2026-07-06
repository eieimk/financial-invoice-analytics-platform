package com.example.invoiceplatform.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SellerSpend(
        String sellerName,
        long invoiceCount,
        BigDecimal totalSpend,
        BigDecimal avgInvoiceAmount
) {
}
