package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.exception.InvalidFileException;
import com.example.invoiceplatform.service.InvoiceUploadService;
import com.example.invoiceplatform.service.InvoiceIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceUploadService invoiceUploadService;

    @MockBean
    private InvoiceIngestionService invoiceIngestionService;

    @Test
    void returns201WithUploadResult_whenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices.csv", "text/csv", "a,b\n".getBytes());
        when(invoiceUploadService.upload(any())).thenReturn(UploadResultResponse.builder()
                .fileName("invoices.csv")
                .s3Key("raw/invoices/uuid-invoices.csv")
                .bucket("test-bucket")
                .sizeBytes(4L)
                .uploadedAt(Instant.now())
                .rowsLoadedToWarehouse(1)
                .warehouseRefreshTriggered(true)
                .build());

        mockMvc.perform(multipart("/api/v1/invoices/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("invoices.csv"))
                .andExpect(jsonPath("$.data.bucket").value("test-bucket"))
                .andExpect(jsonPath("$.data.rowsLoadedToWarehouse").value(1))
                .andExpect(jsonPath("$.data.warehouseRefreshTriggered").value(true));
    }

    @Test
    void returns400WithErrorBody_whenFileIsInvalid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices.pdf", "application/pdf", "x".getBytes());
        when(invoiceUploadService.upload(any()))
                .thenThrow(new InvalidFileException("Only CSV files are supported, got: invoices.pdf"));

        mockMvc.perform(multipart("/api/v1/invoices/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_FILE"));
    }

    @Test
    void returns400_whenFilePartIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/invoices/upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_FILE"));
    }

    @Test
    void returns200WithReconciliationResults_whenParseSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices.csv", "text/csv", "JSON_DATA\n".getBytes());
        when(invoiceIngestionService.ingest(any())).thenReturn(List.of(InvoiceReconciliationResult.builder()
                .rowNumber(1L)
                .invoiceNumber("84652373")
                .sellerName("Nguyen-Roach")
                .clientName("Clark-Foster")
                .lineItemSum(new BigDecimal("232.95"))
                .statedTotal(new BigDecimal("232.95"))
                .discrepancy(false)
                .difference(BigDecimal.ZERO)
                .build()));

        mockMvc.perform(multipart("/api/v1/invoices/parse").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].invoiceNumber").value("84652373"))
                .andExpect(jsonPath("$.data[0].discrepancy").value(false));
    }
}
