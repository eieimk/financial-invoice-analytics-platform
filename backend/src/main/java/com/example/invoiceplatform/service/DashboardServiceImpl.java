package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceAnalyticsRepository analyticsRepository;

    @Override
    public DashboardResponse getDashboard() {
        DashboardMetrics metrics = analyticsRepository.fetchDashboardMetrics();
        return DashboardResponse.builder()
                .totalInvoices(metrics.totalInvoices())
                .totalRevenue(metrics.totalRevenue())
                .averageInvoiceAmount(metrics.averageInvoiceAmount())
                .topVendor(metrics.topVendor())
                .build();
    }
}
