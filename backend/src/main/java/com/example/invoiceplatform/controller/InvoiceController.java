package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.ApiResponse;
import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.service.InvoiceIngestionService;
import com.example.invoiceplatform.service.InvoiceUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Invoices", description = "Invoice file ingestion")
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUploadService invoiceUploadService;
    private final InvoiceIngestionService invoiceIngestionService;

    @Operation(summary = "Upload an invoice CSV (single JSON_DATA column): lands in S3 and the warehouse")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResultResponse>> upload(
            @RequestPart("file") MultipartFile file) {
        UploadResultResponse result = invoiceUploadService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", result));
    }

    @Operation(summary = "Parse an invoice CSV and reconcile the JSON extraction against the raw OCR text")
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<InvoiceReconciliationResult>>> parse(
            @RequestPart("file") MultipartFile file) {
        List<InvoiceReconciliationResult> results = invoiceIngestionService.ingest(file);
        return ResponseEntity.ok(ApiResponse.success("File parsed and reconciled", results));
    }
}
