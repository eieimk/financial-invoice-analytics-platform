package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.AgingBucketResponse;
import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.dto.MonthlySpendResponse;
import com.example.invoiceplatform.dto.ProductLineResponse;
import com.example.invoiceplatform.dto.SellerSpendResponse;
import com.example.invoiceplatform.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void returnsDashboardMetrics() throws Exception {
        when(dashboardService.getDashboard()).thenReturn(DashboardResponse.builder()
                .totalInvoices(1248L)
                .totalRevenue(new BigDecimal("4823650.75"))
                .averageInvoiceAmount(new BigDecimal("3865.10"))
                .topVendor("Patel, Thompson and Montgomery")
                .build());

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInvoices").value(1248))
                .andExpect(jsonPath("$.data.totalRevenue").value(4823650.75))
                .andExpect(jsonPath("$.data.topVendor").value("Patel, Thompson and Montgomery"));
    }

    @Test
    void returnsSpendBySeller_passingLimitParam() throws Exception {
        when(dashboardService.getSpendBySeller(5)).thenReturn(List.of(SellerSpendResponse.builder()
                .sellerName("Cascade Industrial")
                .invoiceCount(2L)
                .totalSpend(new BigDecimal("960.00"))
                .avgInvoiceAmount(new BigDecimal("480.00"))
                .build()));

        mockMvc.perform(get("/api/v1/dashboard/spend-by-seller").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sellerName").value("Cascade Industrial"))
                .andExpect(jsonPath("$.data[0].totalSpend").value(960.00));

        verify(dashboardService).getSpendBySeller(5);
    }

    @Test
    void returnsMonthlyTrend() throws Exception {
        when(dashboardService.getMonthlyTrend()).thenReturn(List.of(MonthlySpendResponse.builder()
                .month("2021-03")
                .totalSpend(new BigDecimal("680.50"))
                .invoiceCount(2L)
                .build()));

        mockMvc.perform(get("/api/v1/dashboard/monthly-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value("2021-03"))
                .andExpect(jsonPath("$.data[0].invoiceCount").value(2));
    }

    @Test
    void returnsProductLines_passingLimitParam() throws Exception {
        when(dashboardService.getProductLines(200)).thenReturn(List.of(ProductLineResponse.builder()
                .description("Hydraulic pump assembly")
                .quantity(new BigDecimal("1.00"))
                .totalPrice(new BigDecimal("300.00"))
                .invoiceDate(java.time.LocalDate.parse("2021-03-20"))
                .sellerName("Cascade Industrial")
                .build()));

        mockMvc.perform(get("/api/v1/dashboard/product-lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].description").value("Hydraulic pump assembly"))
                .andExpect(jsonPath("$.data[0].sellerName").value("Cascade Industrial"))
                .andExpect(jsonPath("$.data[0].invoiceDate").value("2021-03-20"));

        verify(dashboardService).getProductLines(200);
    }

    @Test
    void returnsInvoiceAging() throws Exception {
        when(dashboardService.getInvoiceAging()).thenReturn(List.of(AgingBucketResponse.builder()
                .bucket("60_PLUS_DAYS")
                .invoiceCount(3L)
                .totalAmount(new BigDecimal("1200.00"))
                .build()));

        mockMvc.perform(get("/api/v1/dashboard/invoice-aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].bucket").value("60_PLUS_DAYS"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(1200.00));
    }
}
