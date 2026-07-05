package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.AgingBucketResponse;
import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.dto.SellerSpendResponse;
import com.example.invoiceplatform.model.AgingBucket;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.model.SellerSpend;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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

    @Test
    void mapsSellerSpend_andClampsLimitToAllowedRange() {
        when(analyticsRepository.fetchSpendBySeller(20)).thenReturn(List.of(SellerSpend.builder()
                .sellerName("Cascade Industrial")
                .invoiceCount(2L)
                .totalSpend(new BigDecimal("960.00"))
                .avgInvoiceAmount(new BigDecimal("480.00"))
                .build()));

        List<SellerSpendResponse> result = dashboardService.getSpendBySeller(500);

        // 500 exceeds the cap; the repository must be asked for at most 20
        verify(analyticsRepository).fetchSpendBySeller(20);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).sellerName()).isEqualTo("Cascade Industrial");
        assertThat(result.get(0).totalSpend()).isEqualByComparingTo("960.00");
    }

    @Test
    void mapsProductLines_andClampsLimit() {
        when(analyticsRepository.fetchProductLines(500)).thenReturn(List.of(
                com.example.invoiceplatform.model.ProductLine.builder()
                        .description("Pressure gauge kit")
                        .quantity(new BigDecimal("3.00"))
                        .totalPrice(new BigDecimal("150.00"))
                        .invoiceDate(java.time.LocalDate.parse("2021-03-20"))
                        .sellerName("Cascade Industrial")
                        .build()));

        var result = dashboardService.getProductLines(9999);

        verify(analyticsRepository).fetchProductLines(500);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Pressure gauge kit");
        assertThat(result.get(0).quantity()).isEqualByComparingTo("3.00");
    }

    @Test
    void mapsInvoiceAgingBuckets() {
        when(analyticsRepository.fetchInvoiceAging()).thenReturn(List.of(AgingBucket.builder()
                .bucket("60_PLUS_DAYS")
                .invoiceCount(3L)
                .totalAmount(new BigDecimal("1200.00"))
                .build()));

        List<AgingBucketResponse> result = dashboardService.getInvoiceAging();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).bucket()).isEqualTo("60_PLUS_DAYS");
        assertThat(result.get(0).invoiceCount()).isEqualTo(3L);
        assertThat(result.get(0).totalAmount()).isEqualByComparingTo("1200.00");
    }
}
