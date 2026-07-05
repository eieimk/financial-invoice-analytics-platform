package com.example.invoiceplatform.repository;

import com.example.invoiceplatform.model.DashboardMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnowflakeInvoiceAnalyticsRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void mapsTotalsAndTopVendor_fromJdbcResults() {
        when(jdbcTemplate.queryForMap(any(String.class), anyMap())).thenReturn(Map.of(
                "TOTAL_INVOICES", 42L,
                "TOTAL_REVENUE", new BigDecimal("100000.50"),
                "AVERAGE_INVOICE_AMOUNT", new BigDecimal("2380.01")
        ));
        when(jdbcTemplate.query(any(String.class), anyMap(), any(ResultSetExtractor.class)))
                .thenReturn("Nguyen-Roach");

        SnowflakeInvoiceAnalyticsRepository repository = new SnowflakeInvoiceAnalyticsRepository(jdbcTemplate);

        DashboardMetrics metrics = repository.fetchDashboardMetrics();

        assertThat(metrics.totalInvoices()).isEqualTo(42L);
        assertThat(metrics.totalRevenue()).isEqualByComparingTo("100000.50");
        assertThat(metrics.averageInvoiceAmount()).isEqualByComparingTo("2380.01");
        assertThat(metrics.topVendor()).isEqualTo("Nguyen-Roach");
    }

    @Test
    void defaultsRevenueToZero_whenJdbcReturnsNull() {
        Map<String, Object> nullTotals = new java.util.HashMap<>();
        nullTotals.put("TOTAL_INVOICES", 0L);
        nullTotals.put("TOTAL_REVENUE", null);
        nullTotals.put("AVERAGE_INVOICE_AMOUNT", null);
        when(jdbcTemplate.queryForMap(any(String.class), anyMap())).thenReturn(nullTotals);
        when(jdbcTemplate.query(any(String.class), anyMap(), any(ResultSetExtractor.class)))
                .thenReturn(null);

        SnowflakeInvoiceAnalyticsRepository repository = new SnowflakeInvoiceAnalyticsRepository(jdbcTemplate);

        DashboardMetrics metrics = repository.fetchDashboardMetrics();

        assertThat(metrics.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.averageInvoiceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.topVendor()).isNull();
    }
}
