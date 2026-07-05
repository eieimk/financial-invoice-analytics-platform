package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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
}
