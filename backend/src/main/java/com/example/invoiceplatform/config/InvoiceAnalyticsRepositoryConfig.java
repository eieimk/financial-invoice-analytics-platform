package com.example.invoiceplatform.config;

import com.example.invoiceplatform.repository.EmptyInvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.InvoiceWarehouseWriter;
import com.example.invoiceplatform.repository.NoOpInvoiceWarehouseWriter;
import com.example.invoiceplatform.repository.SnowflakeInvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.SnowflakeInvoiceWarehouseWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Picks the analytics repository implementation: Snowflake when
 * app.snowflake.account is set (SNOWFLAKE_ACCOUNT env var), an empty
 * zeros/no-rows fallback otherwise. The service layer only ever depends on
 * the interface.
 */
@Configuration
public class InvoiceAnalyticsRepositoryConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.snowflake", name = "account")
    public InvoiceAnalyticsRepository snowflakeInvoiceAnalyticsRepository(
            NamedParameterJdbcTemplate snowflakeJdbcTemplate) {
        return new SnowflakeInvoiceAnalyticsRepository(snowflakeJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(InvoiceAnalyticsRepository.class)
    public InvoiceAnalyticsRepository emptyInvoiceAnalyticsRepository() {
        return new EmptyInvoiceAnalyticsRepository();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.snowflake", name = "account")
    public InvoiceWarehouseWriter snowflakeInvoiceWarehouseWriter(
            NamedParameterJdbcTemplate snowflakeJdbcTemplate) {
        return new SnowflakeInvoiceWarehouseWriter(snowflakeJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(InvoiceWarehouseWriter.class)
    public InvoiceWarehouseWriter noOpInvoiceWarehouseWriter() {
        return new NoOpInvoiceWarehouseWriter();
    }
}
