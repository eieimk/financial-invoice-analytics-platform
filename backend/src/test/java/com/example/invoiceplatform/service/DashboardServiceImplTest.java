package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private InvoiceAnalyticsRepository analyticsRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void mapsRepositoryMetricsToResponse() {
        when(analyticsRepository.fetchDashboardMetrics()).thenReturn(DashboardMetrics.builder()
                .totalInvoices(10L)
                .totalRevenue(new BigDecimal("1500.50"))
                .averageInvoiceAmount(new BigDecimal("150.05"))
                .topVendor("Acme Corp")
                .build());

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalInvoices()).isEqualTo(10L);
        assertThat(response.totalRevenue()).isEqualByComparingTo("1500.50");
        assertThat(response.averageInvoiceAmount()).isEqualByComparingTo("150.05");
        assertThat(response.topVendor()).isEqualTo("Acme Corp");
    }
}
