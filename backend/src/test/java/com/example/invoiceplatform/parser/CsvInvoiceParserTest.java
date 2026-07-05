package com.example.invoiceplatform.parser;

import com.example.invoiceplatform.exception.InvoiceParsingException;
import com.example.invoiceplatform.model.ParsedInvoiceRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvInvoiceParserTest {

    private final CsvInvoiceParser parser = new CsvInvoiceParser(new ObjectMapper());

    private static final String SAMPLE_JSON = "{\"invoice\":{\"client_name\":\"Clark-Foster\","
            + "\"client_address\":\"77477 Troy Cliff\",\"seller_name\":\"Nguyen-Roach\","
            + "\"seller_address\":\"247 David Highway\",\"invoice_number\":\"84652373\","
            + "\"invoice_date\":\"02/23/2021\",\"due_date\":\"\"},"
            + "\"items\":[{\"description\":\"Wine Rack\",\"quantity\":\"1.00\",\"total_price\":\"46.55\"}],"
            + "\"subtotal\":{\"tax\":\"21.18\",\"discount\":\"\",\"total\":\"232.95\"},"
            + "\"payment_instructions\":{\"due_date\":\"\",\"bank_name\":\"\","
            + "\"account_number\":\"\",\"payment_method\":\"\"}}";

    private static final String SAMPLE_CSV = "File Name,Json Data,OCRed Text\n"
            + "batch1-0494.jpg,\"" + SAMPLE_JSON.replace("\"", "\"\"") + "\",\"Total $ 211,77 $ 21,18 $ 232,95\"\n";

    @Test
    void supports_returnsTrue_forCsvFile() {
        MockMultipartFile file = new MockMultipartFile("file", "invoices_ocr.csv", "text/csv", new byte[0]);
        assertThat(parser.supports(file)).isTrue();
    }

    @Test
    void supports_returnsFalse_forNonCsvFile() {
        MockMultipartFile file = new MockMultipartFile("file", "invoices.pdf", "application/pdf", new byte[0]);
        assertThat(parser.supports(file)).isFalse();
    }

    @Test
    void parse_extractsStructuredRecordAndKeepsRawOcrText() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices_ocr.csv", "text/csv", SAMPLE_CSV.getBytes());

        List<ParsedInvoiceRecord> records = parser.parse(file);

        assertThat(records).hasSize(1);
        ParsedInvoiceRecord record = records.get(0);
        assertThat(record.sourceFileName()).isEqualTo("batch1-0494.jpg");
        assertThat(record.extraction().invoice().invoiceNumber()).isEqualTo("84652373");
        assertThat(record.extraction().subtotal().total()).isEqualTo("232.95");
        assertThat(record.ocrText()).contains("232,95");
    }

    @Test
    void parse_throwsInvoiceParsingException_whenRequiredColumnMissing() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices_ocr.csv", "text/csv", "File Name,Json Data\nfoo,{}\n".getBytes());

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(InvoiceParsingException.class)
                .hasMessageContaining("OCRed Text");
    }

    @Test
    void parse_throwsInvoiceParsingException_whenJsonDataMalformed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoices_ocr.csv", "text/csv",
                "File Name,Json Data,OCRed Text\nfoo,not-json,some text\n".getBytes());

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(InvoiceParsingException.class)
                .hasMessageContaining("foo");
    }
}
