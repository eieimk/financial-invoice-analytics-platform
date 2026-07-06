package com.example.invoiceplatform.util;

import java.math.BigDecimal;

/**
 * Money fields in the OCR extraction JSON are period-decimal strings that can
 * legitimately be blank (""), so parsing is centralized here instead of raw
 * BigDecimal constructors scattered through the reconciliation logic.
 */
public final class MoneyParser {

    private MoneyParser() {
    }

    public static BigDecimal parseJsonAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
