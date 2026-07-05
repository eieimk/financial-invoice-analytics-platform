package com.example.invoiceplatform.parser;

import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Strategy interface for turning a raw source file into structured invoice records.
 * New source formats (e.g. a direct JSON export) plug in as another implementation
 * without touching the ingestion orchestration in InvoiceIngestionService.
 */
public interface InvoiceParser {

    boolean supports(MultipartFile file);

    List<ParsedInvoiceRecord> parse(MultipartFile file);
}
