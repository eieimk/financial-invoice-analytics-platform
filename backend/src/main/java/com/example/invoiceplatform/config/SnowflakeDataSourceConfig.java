package com.example.invoiceplatform.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * Only activates once real Snowflake credentials are present (app.snowflake.account
 * set via SNOWFLAKE_ACCOUNT). Local dev without credentials falls back to
 * MockInvoiceAnalyticsRepository - see InvoiceAnalyticsRepositoryConfig.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.snowflake", name = "account")
public class SnowflakeDataSourceConfig {

    @Bean
    public DataSource snowflakeDataSource(SnowflakeProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("net.snowflake.client.jdbc.SnowflakeDriver");
        dataSource.setJdbcUrl("jdbc:snowflake://%s.snowflakecomputing.com/".formatted(properties.account()));
        dataSource.setUsername(properties.user());
        dataSource.setPassword(properties.password());
        dataSource.addDataSourceProperty("warehouse", properties.warehouse());
        dataSource.addDataSourceProperty("db", properties.database());
        dataSource.addDataSourceProperty("schema", properties.schema());
        // Snowflake bills per-second warehouse compute; keep the pool small so an
        // idle backend isn't holding warehouse sessions open for no reason.
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    @Bean
    public NamedParameterJdbcTemplate snowflakeJdbcTemplate(DataSource snowflakeDataSource) {
        return new NamedParameterJdbcTemplate(snowflakeDataSource);
    }
}
