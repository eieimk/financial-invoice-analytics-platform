package com.example.invoiceplatform.util;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The two sources disagree on decimal notation: the JSON extraction uses
 * period-decimal ("232.95"), the raw OCR text uses comma-decimal ("232,95")
 * because the source images are European-format invoices. Centralizing the
 * parsing here is what lets InvoiceReconciliationService compare them.
 */
public final class MoneyParser {

    private static final Pattern LAST_AMOUNT = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2})");

    private MoneyParser() {
    }

    public static BigDecimal parseJsonAmount(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    public static BigDecimal parseCommaDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim()
                .replace(".", "")
                .replace(",", ".");
        return new BigDecimal(normalized);
    }

    /**
     * Pulls the last money-shaped token out of free-form OCR text, e.g. the grand
     * total at the end of a "Total $ 211,77 $ 21,18 $ 232,95" summary line.
     */
    public static BigDecimal extractLastAmount(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return null;
        }
        Matcher matcher = LAST_AMOUNT.matcher(ocrText);
        String last = null;
        while (matcher.find()) {
            last = matcher.group(1);
        }
        return last == null ? null : parseCommaDecimal(last);
    }
}
