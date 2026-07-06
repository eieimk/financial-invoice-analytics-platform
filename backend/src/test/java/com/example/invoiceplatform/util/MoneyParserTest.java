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
    void returnsNull_whenJsonAmountUnparseable() {
        assertThat(MoneyParser.parseJsonAmount("12,50")).isNull();
        assertThat(MoneyParser.parseJsonAmount("abc")).isNull();
    }
}
