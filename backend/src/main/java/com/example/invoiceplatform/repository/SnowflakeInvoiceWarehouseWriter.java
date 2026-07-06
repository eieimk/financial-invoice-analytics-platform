package com.example.invoiceplatform.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SnowflakeInvoiceWarehouseWriter implements InvoiceWarehouseWriter {

    private static final String INSERT_SQL =
            "INSERT INTO raw_invoice_ocr (json_data) VALUES (:json)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int insertRawInvoices(List<String> jsonRows) {
        SqlParameterSource[] batch = jsonRows.stream()
                .map(json -> new MapSqlParameterSource("json", json))
                .toArray(SqlParameterSource[]::new);
        int[] counts = jdbcTemplate.batchUpdate(INSERT_SQL, batch);
        int inserted = 0;
        for (int c : counts) {
            inserted += Math.max(c, 0);
        }
        log.info("Landed {} raw invoice rows in Snowflake", inserted);
        return inserted;
    }

    @Override
    public boolean triggerLoadTasks() {
        try {
            // EXECUTE TASK is async on Snowflake's side; this just schedules an
            // immediate run instead of waiting for the 5-minute cron.
            jdbcTemplate.getJdbcOperations().execute("EXECUTE TASK load_star_schema_task");
            jdbcTemplate.getJdbcOperations().execute("EXECUTE TASK reconciliation_exception_task");
            return true;
        } catch (Exception e) {
            // Not fatal: the scheduled tasks pick the rows up within 5 minutes.
            log.warn("Could not trigger warehouse load tasks immediately: {}", e.getMessage());
            return false;
        }
    }
}
