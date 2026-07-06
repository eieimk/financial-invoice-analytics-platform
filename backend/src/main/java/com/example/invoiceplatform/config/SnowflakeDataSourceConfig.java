package com.example.invoiceplatform.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Only activates once real Snowflake credentials are present (app.snowflake.account
 * set via SNOWFLAKE_ACCOUNT). Local dev without credentials falls back to
 * EmptyInvoiceAnalyticsRepository - see InvoiceAnalyticsRepositoryConfig.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.snowflake", name = "account")
public class SnowflakeDataSourceConfig {

    @Bean
    public DataSource snowflakeDataSource(SnowflakeProperties properties) {
        if (!StringUtils.hasText(properties.warehouse())
                || !StringUtils.hasText(properties.database())
                || !StringUtils.hasText(properties.schema())) {
            throw new IllegalStateException(
                    "SNOWFLAKE_ACCOUNT is set but SNOWFLAKE_WAREHOUSE/SNOWFLAKE_DB/SNOWFLAKE_SCHEMA "
                            + "are incomplete - the JDBC driver would connect with no active "
                            + "warehouse and every query would fail. Set all three or unset "
                            + "SNOWFLAKE_ACCOUNT to fall back to the mock repository.");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("net.snowflake.client.jdbc.SnowflakeDriver");
        dataSource.setJdbcUrl("jdbc:snowflake://%s.snowflakecomputing.com/".formatted(properties.account()));
        dataSource.setUsername(properties.user());
        dataSource.setPassword(properties.password());
        dataSource.addDataSourceProperty("warehouse", properties.warehouse());
        dataSource.addDataSourceProperty("db", properties.database());
        dataSource.addDataSourceProperty("schema", properties.schema());
        // The driver's default Arrow result format needs deep reflection into
        // java.nio, which Java 17+ blocks unless the JVM is started with
        // --add-opens=java.base/java.nio=ALL-UNNAMED. JSON results avoid the
        // flag entirely; for this app's small aggregate result sets the Arrow
        // performance advantage is irrelevant.
        dataSource.addDataSourceProperty("JDBC_QUERY_RESULT_FORMAT", "JSON");
        // The driver silently ignores a warehouse the role can't USE and connects
        // with no active warehouse; this makes that misconfiguration fail at
        // startup with a clear message instead of on the first dashboard query.
        dataSource.setConnectionInitSql("USE WAREHOUSE " + properties.warehouse());
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
