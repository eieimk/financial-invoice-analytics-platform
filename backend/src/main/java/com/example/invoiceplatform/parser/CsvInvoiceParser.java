package com.example.invoiceplatform.parser;

import com.example.invoiceplatform.exception.InvoiceParsingException;
import com.example.invoiceplatform.model.InvoiceJsonPayload;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses the upload CSV: a single JSON_DATA column, one structured invoice
 * extraction per row (matches the raw_invoice_ocr landing table, which also
 * keeps only the JSON). Header lookup is case-insensitive so JSON_DATA /
 * json_data both work.
 */
@Component
@RequiredArgsConstructor
public class CsvInvoiceParser implements InvoiceParser {

    private static final String JSON_COLUMN = "json_data";

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    @Override
    public List<ParsedInvoiceRecord> parse(MultipartFile file) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            String jsonHeader = parser.getHeaderNames().stream()
                    .filter(h -> h.trim().toLowerCase(Locale.ROOT).equals(JSON_COLUMN))
                    .findFirst()
                    .orElseThrow(() -> new InvoiceParsingException(
                            "Missing required column 'JSON_DATA' in " + file.getOriginalFilename(), null));

            List<ParsedInvoiceRecord> records = new ArrayList<>();
            for (CSVRecord row : parser) {
                records.add(toRecord(row, jsonHeader));
            }
            return records;
        } catch (IOException e) {
            throw new InvoiceParsingException("Failed to read CSV file " + file.getOriginalFilename(), e);
        }
    }

    private ParsedInvoiceRecord toRecord(CSVRecord row, String jsonHeader) {
        String rawJson = row.get(jsonHeader);
        try {
            InvoiceJsonPayload extraction = objectMapper.readValue(rawJson, InvoiceJsonPayload.class);
            return new ParsedInvoiceRecord(row.getRecordNumber(), rawJson, extraction);
        } catch (IOException e) {
            throw new InvoiceParsingException(
                    "Malformed JSON_DATA at row " + row.getRecordNumber(), e);
        }
    }
}
