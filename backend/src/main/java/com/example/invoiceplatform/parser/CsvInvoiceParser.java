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
 * Parses the OCR invoice-extraction CSV: one row per scanned invoice image, with a
 * structured JSON extraction column and the raw OCR text column side by side so
 * downstream reconciliation can cross-check one against the other.
 */
@Component
@RequiredArgsConstructor
public class CsvInvoiceParser implements InvoiceParser {

    private static final List<String> REQUIRED_HEADERS = List.of("File Name", "Json Data", "OCRed Text");

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

            for (String required : REQUIRED_HEADERS) {
                if (!parser.getHeaderNames().contains(required)) {
                    throw new InvoiceParsingException(
                            "Missing required column '" + required + "' in " + file.getOriginalFilename(), null);
                }
            }

            List<ParsedInvoiceRecord> records = new ArrayList<>();
            for (CSVRecord row : parser) {
                records.add(toRecord(row));
            }
            return records;
        } catch (IOException e) {
            throw new InvoiceParsingException("Failed to read CSV file " + file.getOriginalFilename(), e);
        }
    }

    private ParsedInvoiceRecord toRecord(CSVRecord row) {
        String fileName = row.get("File Name");
        String rawJson = row.get("Json Data");
        String ocrText = row.get("OCRed Text");

        InvoiceJsonPayload extraction;
        try {
            extraction = objectMapper.readValue(rawJson, InvoiceJsonPayload.class);
        } catch (IOException e) {
            throw new InvoiceParsingException(
                    "Malformed 'Json Data' for row " + fileName + " (line " + row.getRecordNumber() + ")", e);
        }
        return new ParsedInvoiceRecord(fileName, extraction, ocrText);
    }
}
