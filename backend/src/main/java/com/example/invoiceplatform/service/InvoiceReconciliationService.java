package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.model.InvoiceLineItem;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.util.MoneyParser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * JSON-internal consistency check, mirroring the warehouse-side
 * reconciliation_exception_task: the extraction's line items should sum to
 * its own stated total (SUM(items[].total_price) == subtotal.total). A gap
 * beyond rounding tolerance means the OCR extraction dropped or misread a
 * line item or the total, and the row needs manual review before it's
 * trusted for AP analytics.
 */
@Service
public class InvoiceReconciliationService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    public InvoiceReconciliationResult reconcile(ParsedInvoiceRecord record) {
        BigDecimal statedTotal = record.extraction().subtotal() == null
                ? null
                : MoneyParser.parseJsonAmount(record.extraction().subtotal().total());
        BigDecimal lineItemSum = sumLineItems(record.extraction().items());

        boolean discrepancy;
        BigDecimal difference;
        if (statedTotal == null || lineItemSum == null) {
            discrepancy = true;
            difference = null;
        } else {
            difference = statedTotal.subtract(lineItemSum).abs().setScale(2, RoundingMode.HALF_UP);
            discrepancy = difference.compareTo(TOLERANCE) > 0;
        }

        return InvoiceReconciliationResult.builder()
                .rowNumber(record.rowNumber())
                .invoiceNumber(record.extraction().invoice() == null ? null : record.extraction().invoice().invoiceNumber())
                .sellerName(record.extraction().invoice() == null ? null : record.extraction().invoice().sellerName())
                .clientName(record.extraction().invoice() == null ? null : record.extraction().invoice().clientName())
                .lineItemSum(lineItemSum)
                .statedTotal(statedTotal)
                .discrepancy(discrepancy)
                .difference(difference)
                .build();
    }

    private BigDecimal sumLineItems(List<InvoiceLineItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceLineItem item : items) {
            BigDecimal price = MoneyParser.parseJsonAmount(item.totalPrice());
            if (price == null) {
                // an unparseable line makes the whole sum untrustworthy
                return null;
            }
            sum = sum.add(price);
        }
        return sum;
    }
}
