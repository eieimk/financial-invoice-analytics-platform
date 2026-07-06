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
 * Orchestrates ingestion: pick the parser strategy that supports this file
 * type and parse it into structured records; optionally reconcile each
 * record's line items against its stated total. Adding a new source format
 * means adding another InvoiceParser bean; this class doesn't change.
 */
@Service
@RequiredArgsConstructor
public class InvoiceIngestionService {

    private final List<InvoiceParser> parsers;
    private final InvoiceReconciliationService reconciliationService;

    public List<ParsedInvoiceRecord> parse(MultipartFile file) {
        InvoiceParser parser = parsers.stream()
                .filter(p -> p.supports(file))
                .findFirst()
                .orElseThrow(() -> new InvalidFileException(
                        "No parser available for file: " + file.getOriginalFilename()));
        return parser.parse(file);
    }

    public List<InvoiceReconciliationResult> ingest(MultipartFile file) {
        return parse(file).stream()
                .map(reconciliationService::reconcile)
                .toList();
    }
}
