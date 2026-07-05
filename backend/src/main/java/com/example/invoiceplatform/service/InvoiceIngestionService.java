package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.InvoiceReconciliationResult;
import com.example.invoiceplatform.exception.InvalidFileException;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.example.invoiceplatform.parser.InvoiceParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Orchestrates ingestion: pick the parser strategy that supports this file type,
 * parse it into structured records, then reconcile each record's JSON extraction
 * against its raw OCR text. Adding a new source format means adding another
 * InvoiceParser bean; this class doesn't change.
 */
@Service
@RequiredArgsConstructor
public class InvoiceIngestionService {

    private final List<InvoiceParser> parsers;
    private final InvoiceReconciliationService reconciliationService;

    public List<InvoiceReconciliationResult> ingest(MultipartFile file) {
        InvoiceParser parser = parsers.stream()
                .filter(p -> p.supports(file))
                .findFirst()
                .orElseThrow(() -> new InvalidFileException(
                        "No parser available for file: " + file.getOriginalFilename()));

        List<ParsedInvoiceRecord> records = parser.parse(file);
        return records.stream()
                .map(reconciliationService::reconcile)
                .toList();
    }
}
