package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.util.MoneyParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cross-checks the structured JSON extraction against the raw OCR text for the
 * same scanned invoice. A mismatch beyond rounding tolerance means the OCR
 * extraction step likely misread a digit and the row needs manual review
 * before it's trusted for AP analytics.
 */
@Service
public class InvoiceReconciliationService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    public InvoiceReconciliationResult reconcile(ParsedInvoiceRecord record) {
        BigDecimal extractedTotal = record.extraction().subtotal() == null
                ? null
                : MoneyParser.parseJsonAmount(record.extraction().subtotal().total());
        BigDecimal ocrTotal = MoneyParser.extractLastAmount(record.ocrText());

        boolean discrepancy;
        BigDecimal difference;
        if (extractedTotal == null || ocrTotal == null) {
            discrepancy = true;
            difference = null;
        } else {
            difference = extractedTotal.subtract(ocrTotal).abs().setScale(2, RoundingMode.HALF_UP);
            discrepancy = difference.compareTo(TOLERANCE) > 0;
        }

        return InvoiceReconciliationResult.builder()
                .sourceFileName(record.sourceFileName())
                .invoiceNumber(record.extraction().invoice() == null ? null : record.extraction().invoice().invoiceNumber())
                .sellerName(record.extraction().invoice() == null ? null : record.extraction().invoice().sellerName())
                .clientName(record.extraction().invoice() == null ? null : record.extraction().invoice().clientName())
                .extractedTotal(extractedTotal)
                .ocrTotal(ocrTotal)
                .discrepancy(discrepancy)
                .difference(difference)
                .build();
    }
}
