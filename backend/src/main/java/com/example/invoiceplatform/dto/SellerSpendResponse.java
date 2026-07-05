package com.example.invoiceplatform.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SellerSpendResponse(
        String sellerName,
        long invoiceCount,
        BigDecimal totalSpend,
        BigDecimal avgInvoiceAmount
) {
}
