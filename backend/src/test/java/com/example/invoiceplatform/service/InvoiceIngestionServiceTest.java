package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.exception.InvalidFileException;
import com.example.invoiceplatform.model.InvoiceHeader;
import com.example.invoiceplatform.model.InvoiceJsonPayload;
import com.example.invoiceplatform.model.InvoiceSubtotal;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.parser.InvoiceParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceIngestionServiceTest {

    @Mock
    private InvoiceParser csvParser;

    @Mock
    private InvoiceParser otherParser;

    private final InvoiceReconciliationService reconciliationService = new InvoiceReconciliationService();

    @Test
    void selectsSupportingParser_andReconcilesEachRecord() {
        MockMultipartFile file = new MockMultipartFile("file", "invoices_ocr.csv", "text/csv", new byte[0]);
        InvoiceJsonPayload payload = new InvoiceJsonPayload(
                new InvoiceHeader("Clark-Foster", "addr", "Nguyen-Roach", "addr", "84652373", "02/23/2021", ""),
                List.of(new com.example.invoiceplatform.model.InvoiceLineItem("Wine Rack", "1.00", "232.95")),
                new InvoiceSubtotal("21.18", "", "232.95"), null);
        ParsedInvoiceRecord record = new ParsedInvoiceRecord(1L, "{}", payload);

        when(otherParser.supports(file)).thenReturn(false);
        when(csvParser.supports(file)).thenReturn(true);
        when(csvParser.parse(file)).thenReturn(List.of(record));

        InvoiceIngestionService service = new InvoiceIngestionService(
                List.of(otherParser, csvParser), reconciliationService);

        List<InvoiceReconciliationResult> results = service.ingest(file);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).invoiceNumber()).isEqualTo("84652373");
        assertThat(results.get(0).discrepancy()).isFalse();
    }

    @Test
    void throwsInvalidFileException_whenNoParserSupportsFile() {
        MockMultipartFile file = new MockMultipartFile("file", "invoices.xlsx", "application/xlsx", new byte[0]);
        when(csvParser.supports(any())).thenReturn(false);

        InvoiceIngestionService service = new InvoiceIngestionService(List.of(csvParser), reconciliationService);

        assertThatThrownBy(() -> service.ingest(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("invoices.xlsx");
    }
}
