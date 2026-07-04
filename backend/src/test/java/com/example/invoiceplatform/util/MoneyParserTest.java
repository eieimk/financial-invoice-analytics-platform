package com.example.invoiceplatform.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyParserTest {

    @Test
    void parsesJsonAmount_withPeriodDecimal() {
        assertThat(MoneyParser.parseJsonAmount("232.95")).isEqualByComparingTo("232.95");
    }

    @Test
    void returnsNull_whenJsonAmountBlank() {
        assertThat(MoneyParser.parseJsonAmount("")).isNull();
        assertThat(MoneyParser.parseJsonAmount(null)).isNull();
    }

    @Test
    void parsesCommaDecimal_convertingToPeriod() {
        assertThat(MoneyParser.parseCommaDecimal("232,95")).isEqualByComparingTo("232.95");
    }

    @Test
    void parsesCommaDecimal_withThousandsSeparator() {
        assertThat(MoneyParser.parseCommaDecimal("1.234,56")).isEqualByComparingTo("1234.56");
    }

    @Test
    void extractsLastAmount_fromOcrSummaryLine() {
        String ocrText = "Total $ 211,77 $ 21,18 $ 232,95";
        assertThat(MoneyParser.extractLastAmount(ocrText)).isEqualByComparingTo("232.95");
    }

    @Test
    void returnsNull_whenOcrTextHasNoAmount() {
        assertThat(MoneyParser.extractLastAmount("no numbers here")).isNull();
        assertThat(MoneyParser.extractLastAmount(null)).isNull();
    }
}
