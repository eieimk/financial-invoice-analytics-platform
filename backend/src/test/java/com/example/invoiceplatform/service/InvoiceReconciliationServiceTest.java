package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.model.InvoiceHeader;
import com.example.invoiceplatform.model.InvoiceJsonPayload;
import com.example.invoiceplatform.model.InvoiceSubtotal;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceReconciliationServiceTest {

    private final InvoiceReconciliationService service = new InvoiceReconciliationService();

    private ParsedInvoiceRecord recordWithTotals(String jsonTotal, String ocrText) {
        InvoiceHeader header = new InvoiceHeader(
                "Clark-Foster", "addr", "Nguyen-Roach", "addr", "84652373", "02/23/2021", "");
        InvoiceSubtotal subtotal = new InvoiceSubtotal("21.18", "", jsonTotal);
        InvoiceJsonPayload payload = new InvoiceJsonPayload(header, List.of(), subtotal, null);
        return new ParsedInvoiceRecord("batch1-0494.jpg", payload, ocrText);
    }

    @Test
    void noDiscrepancy_whenTotalsMatchWithinTolerance() {
        InvoiceReconciliationResult result = service.reconcile(
                recordWithTotals("232.95", "Total $ 211,77 $ 21,18 $ 232,95"));

        assertThat(result.discrepancy()).isFalse();
        assertThat(result.extractedTotal()).isEqualByComparingTo("232.95");
        assertThat(result.ocrTotal()).isEqualByComparingTo("232.95");
        assertThat(result.difference()).isEqualByComparingTo("0.00");
    }

    @Test
    void flagsDiscrepancy_whenTotalsDiffer() {
        InvoiceReconciliationResult result = service.reconcile(
                recordWithTotals("232.95", "Total $ 211,77 $ 21,18 $ 999,00"));

        assertThat(result.discrepancy()).isTrue();
        assertThat(result.difference()).isEqualByComparingTo(new BigDecimal("766.05"));
    }

    @Test
    void flagsDiscrepancy_whenOcrTotalMissing() {
        InvoiceReconciliationResult result = service.reconcile(recordWithTotals("232.95", "no total here"));

        assertThat(result.discrepancy()).isTrue();
        assertThat(result.ocrTotal()).isNull();
    }

    @Test
    void carriesInvoiceHeaderFields_intoResult() {
        InvoiceReconciliationResult result = service.reconcile(
                recordWithTotals("232.95", "Total $ 211,77 $ 21,18 $ 232,95"));

        assertThat(result.invoiceNumber()).isEqualTo("84652373");
        assertThat(result.sellerName()).isEqualTo("Nguyen-Roach");
        assertThat(result.clientName()).isEqualTo("Clark-Foster");
        assertThat(result.sourceFileName()).isEqualTo("batch1-0494.jpg");
    }
}
