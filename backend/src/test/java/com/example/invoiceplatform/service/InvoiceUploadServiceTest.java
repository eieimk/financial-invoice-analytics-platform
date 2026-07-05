package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.exception.InvoiceParsingException;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.repository.InvoiceWarehouseWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceUploadServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private InvoiceIngestionService ingestionService;

    @Mock
    private InvoiceWarehouseWriter warehouseWriter;

    @InjectMocks
    private InvoiceUploadService uploadService;

    private final MockMultipartFile file = new MockMultipartFile(
            "file", "invoices.csv", "text/csv", "JSON_DATA\n\"{}\"\n".getBytes());

    private UploadResultResponse storedResult() {
        return UploadResultResponse.builder()
                .fileName("invoices.csv")
                .s3Key("invoices/uuid-invoices.csv")
                .bucket("invoices-buckets")
                .sizeBytes(12L)
                .uploadedAt(Instant.now())
                .build();
    }

    @Test
    void landsRowsInWarehouse_andTriggersTasks_afterS3Store() {
        when(ingestionService.parse(file)).thenReturn(List.of(
                new ParsedInvoiceRecord(1L, "{\"a\":1}", null),
                new ParsedInvoiceRecord(2L, "{\"b\":2}", null)));
        when(fileStorageService.store(file)).thenReturn(storedResult());
        when(warehouseWriter.insertRawInvoices(List.of("{\"a\":1}", "{\"b\":2}"))).thenReturn(2);
        when(warehouseWriter.triggerLoadTasks()).thenReturn(true);

        UploadResultResponse result = uploadService.upload(file);

        assertThat(result.rowsLoadedToWarehouse()).isEqualTo(2);
        assertThat(result.warehouseRefreshTriggered()).isTrue();
        assertThat(result.s3Key()).isEqualTo("invoices/uuid-invoices.csv");
    }

    @Test
    void skipsS3AndWarehouse_whenFileDoesNotParse() {
        when(ingestionService.parse(file)).thenThrow(new InvoiceParsingException("Malformed JSON_DATA at row 1", null));

        assertThatThrownBy(() -> uploadService.upload(file))
                .isInstanceOf(InvoiceParsingException.class);

        verify(fileStorageService, never()).store(any());
        verify(warehouseWriter, never()).insertRawInvoices(any());
    }

    @Test
    void doesNotTriggerTasks_whenNoRowsLanded() {
        when(ingestionService.parse(file)).thenReturn(List.of(new ParsedInvoiceRecord(1L, "{}", null)));
        when(fileStorageService.store(file)).thenReturn(storedResult());
        when(warehouseWriter.insertRawInvoices(any())).thenReturn(0);

        UploadResultResponse result = uploadService.upload(file);

        verify(warehouseWriter, never()).triggerLoadTasks();
        assertThat(result.warehouseRefreshTriggered()).isFalse();
    }
}
