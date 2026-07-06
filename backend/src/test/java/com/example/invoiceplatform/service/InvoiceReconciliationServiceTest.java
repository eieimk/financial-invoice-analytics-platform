package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.model.InvoiceHeader;
import com.example.invoiceplatform.model.InvoiceJsonPayload;
import com.example.invoiceplatform.model.InvoiceLineItem;
import com.example.invoiceplatform.model.InvoiceSubtotal;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceReconciliationServiceTest {

    private final InvoiceReconciliationService service = new InvoiceReconciliationService();

    private ParsedInvoiceRecord record(String statedTotal, String... linePrices) {
        InvoiceHeader header = new InvoiceHeader(
                "Clark-Foster", "addr", "Nguyen-Roach", "addr", "84652373", "02/23/2021", "");
        List<InvoiceLineItem> items = List.of(linePrices).stream()
                .map(p -> new InvoiceLineItem("item", "1.00", p))
                .toList();
        InvoiceSubtotal subtotal = new InvoiceSubtotal("21.18", "", statedTotal);
        InvoiceJsonPayload payload = new InvoiceJsonPayload(header, items, subtotal, null);
        return new ParsedInvoiceRecord(1L, "{}", payload);
    }

    @Test
    void noDiscrepancy_whenLineItemsSumToStatedTotal() {
        InvoiceReconciliationResult result = service.reconcile(record("232.95", "46.55", "15.40", "39.00", "110.00", "22.00"));

        assertThat(result.discrepancy()).isFalse();
        assertThat(result.lineItemSum()).isEqualByComparingTo("232.95");
        assertThat(result.statedTotal()).isEqualByComparingTo("232.95");
        assertThat(result.difference()).isEqualByComparingTo("0.00");
    }

    @Test
    void flagsDiscrepancy_whenLineSumDiffersFromStatedTotal() {
        InvoiceReconciliationResult result = service.reconcile(record("480.00", "300.00", "150.00"));

        assertThat(result.discrepancy()).isTrue();
        assertThat(result.difference()).isEqualByComparingTo("30.00");
    }

    @Test
    void flagsDiscrepancy_whenStatedTotalMissing() {
        InvoiceReconciliationResult result = service.reconcile(record("", "46.55"));

        assertThat(result.discrepancy()).isTrue();
        assertThat(result.statedTotal()).isNull();
        assertThat(result.difference()).isNull();
    }

    @Test
    void flagsDiscrepancy_whenALinePriceIsUnparseable() {
        InvoiceReconciliationResult result = service.reconcile(record("100.00", "50.00", "not-a-number"));

        assertThat(result.discrepancy()).isTrue();
        assertThat(result.lineItemSum()).isNull();
    }

    @Test
    void carriesInvoiceHeaderFields_intoResult() {
        InvoiceReconciliationResult result = service.reconcile(record("46.55", "46.55"));

        assertThat(result.rowNumber()).isEqualTo(1L);
        assertThat(result.invoiceNumber()).isEqualTo("84652373");
        assertThat(result.sellerName()).isEqualTo("Nguyen-Roach");
        assertThat(result.clientName()).isEqualTo("Clark-Foster");
    }
}
