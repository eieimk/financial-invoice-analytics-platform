package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.repository.InvoiceWarehouseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Upload flow: land the raw file in S3 (immutable audit copy), then land the
 * parsed rows in the warehouse over JDBC and kick the load tasks so the
 * dashboard reflects the upload in seconds. In production the JDBC write
 * would be replaced by S3 -> Snowpipe auto-ingest; the S3 step is already
 * production-shaped, which is why it stays first.
 *
 * The file is parsed *before* the S3 store so a malformed CSV fails the whole
 * request without leaving an orphan object in the landing zone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceUploadService {

    private final FileStorageService fileStorageService;
    private final InvoiceIngestionService ingestionService;
    private final InvoiceWarehouseWriter warehouseWriter;

    public UploadResultResponse upload(MultipartFile file) {
        List<ParsedInvoiceRecord> records = ingestionService.parse(file);

        UploadResultResponse stored = fileStorageService.store(file);

        int rowsLoaded = warehouseWriter.insertRawInvoices(
                records.stream().map(ParsedInvoiceRecord::rawJson).toList());
        boolean triggered = rowsLoaded > 0 && warehouseWriter.triggerLoadTasks();

        return UploadResultResponse.builder()
                .fileName(stored.fileName())
                .s3Key(stored.s3Key())
                .bucket(stored.bucket())
                .sizeBytes(stored.sizeBytes())
                .uploadedAt(stored.uploadedAt())
                .rowsLoadedToWarehouse(rowsLoaded)
                .warehouseRefreshTriggered(triggered)
                .build();
    }
}
