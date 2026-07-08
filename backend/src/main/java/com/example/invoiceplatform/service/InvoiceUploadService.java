package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.UploadResultResponse;
import com.example.invoiceplatform.repository.InvoiceWarehouseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload flow: land the raw file in S3 (immutable audit copy). Warehouse
 * ingestion is Snowpipe's job now (auto-ingest off the S3 event notification,
 * see snowflake/pipe/create_pipe.sql) — this service deliberately does NOT
 * also insert into raw_invoice_ocr over JDBC anymore. It used to, as a demo
 * stand-in before the AWS event-notification wiring existed, but doing both
 * double-lands every upload (once via JDBC, once via Snowpipe), which
 * silently double-counts spend/invoice-count in the dynamic-table read path
 * (item_detail/daily_summary have no dedup logic, unlike the star-schema
 * MERGE). Now that Snowpipe is live, JDBC insert is redundant — the
 * warehouse writer is only still used to nudge the load tasks below.
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
        // Parsed only to validate the file fails fast on bad input; the raw
        // JSON itself no longer goes into raw_invoice_ocr here — Snowpipe
        // does that off the S3 object stored below.
        ingestionService.parse(file);

        UploadResultResponse stored = fileStorageService.store(file);

        // Best-effort nudge: harmless even if Snowpipe hasn't landed the row
        // yet, since the tasks' own WHEN SYSTEM$STREAM_HAS_DATA guard just
        // no-ops when there's nothing new to process.
        boolean triggered = warehouseWriter.triggerLoadTasks();

        return UploadResultResponse.builder()
                .fileName(stored.fileName())
                .s3Key(stored.s3Key())
                .bucket(stored.bucket())
                .sizeBytes(stored.sizeBytes())
                .uploadedAt(stored.uploadedAt())
                .warehouseRefreshTriggered(triggered)
                .build();
    }
}
