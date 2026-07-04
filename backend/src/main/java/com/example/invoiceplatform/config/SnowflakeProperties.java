package com.example.invoiceplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Snowflake JDBC connection settings. Bound now so configuration is in place;
 * the actual DataSource/JdbcTemplate wiring lands with the Snowflake integration.
 */
@ConfigurationProperties(prefix = "app.snowflake")
public record SnowflakeProperties(
        String account,
        String user,
        String password,
        String warehouse,
        String database,
        String schema
) {
}
