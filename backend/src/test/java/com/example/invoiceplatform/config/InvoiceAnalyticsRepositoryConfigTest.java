package com.example.invoiceplatform.config;

import com.example.invoiceplatform.repository.EmptyInvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.SnowflakeInvoiceAnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InvoiceAnalyticsRepositoryConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(InvoiceAnalyticsRepositoryConfig.class);

    @Test
    void usesEmptyRepository_whenNoSnowflakeAccountConfigured() {
        contextRunner.run(context ->
                assertThat(context.getBean(InvoiceAnalyticsRepository.class))
                        .isInstanceOf(EmptyInvoiceAnalyticsRepository.class));
    }

    @Test
    void usesSnowflakeRepository_whenAccountConfiguredAndJdbcTemplateAvailable() {
        contextRunner
                .withPropertyValues("app.snowflake.account=xy12345")
                .withBean(NamedParameterJdbcTemplate.class, () -> mock(NamedParameterJdbcTemplate.class))
                .run(context ->
                        assertThat(context.getBean(InvoiceAnalyticsRepository.class))
                                .isInstanceOf(SnowflakeInvoiceAnalyticsRepository.class));
    }
}
