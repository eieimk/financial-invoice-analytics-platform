package com.example.invoiceplatform.config;

import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.MockInvoiceAnalyticsRepository;
import com.example.invoiceplatform.repository.SnowflakeInvoiceAnalyticsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Picks the analytics repository implementation: Snowflake when
 * app.snowflake.account is set (SNOWFLAKE_ACCOUNT env var), the in-memory
 * mock otherwise. The service layer only ever depends on the interface.
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
    public InvoiceAnalyticsRepository mockInvoiceAnalyticsRepository() {
        return new MockInvoiceAnalyticsRepository();
    }
}
