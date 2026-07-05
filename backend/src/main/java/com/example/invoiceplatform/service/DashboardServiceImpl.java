package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.AgingBucketResponse;
import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.dto.MonthlySpendResponse;
import com.example.invoiceplatform.dto.ProductLineResponse;
import com.example.invoiceplatform.dto.SellerSpendResponse;
import com.example.invoiceplatform.model.DashboardMetrics;
import com.example.invoiceplatform.repository.InvoiceAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    // The seed/demo dataset has a handful of sellers; cap the chart so a large
    // real dataset can't return an unbounded series (the >N tail belongs in
    // "Other" or a paginated report, not the headline chart).
    private static final int MAX_SELLER_LIMIT = 20;

    // Line items feed the scatter/word-cloud charts point-by-point; cap high
    // enough for a dense scatter but bounded so the payload can't explode.
    private static final int MAX_PRODUCT_LINE_LIMIT = 500;

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

    @Override
    public List<SellerSpendResponse> getSpendBySeller(int limit) {
        int effectiveLimit = Math.clamp(limit, 1, MAX_SELLER_LIMIT);
        return analyticsRepository.fetchSpendBySeller(effectiveLimit).stream()
                .map(s -> SellerSpendResponse.builder()
                        .sellerName(s.sellerName())
                        .invoiceCount(s.invoiceCount())
                        .totalSpend(s.totalSpend())
                        .avgInvoiceAmount(s.avgInvoiceAmount())
                        .build())
                .toList();
    }

    @Override
    public List<MonthlySpendResponse> getMonthlyTrend() {
        return analyticsRepository.fetchMonthlyTrend().stream()
                .map(m -> MonthlySpendResponse.builder()
                        .month(m.month())
                        .totalSpend(m.totalSpend())
                        .invoiceCount(m.invoiceCount())
                        .build())
                .toList();
    }

    @Override
    public List<ProductLineResponse> getProductLines(int limit) {
        int effectiveLimit = Math.clamp(limit, 1, MAX_PRODUCT_LINE_LIMIT);
        return analyticsRepository.fetchProductLines(effectiveLimit).stream()
                .map(l -> ProductLineResponse.builder()
                        .description(l.description())
                        .quantity(l.quantity())
                        .totalPrice(l.totalPrice())
                        .invoiceDate(l.invoiceDate())
                        .sellerName(l.sellerName())
                        .build())
                .toList();
    }

    @Override
    public List<AgingBucketResponse> getInvoiceAging() {
        return analyticsRepository.fetchInvoiceAging().stream()
                .map(b -> AgingBucketResponse.builder()
                        .bucket(b.bucket())
                        .invoiceCount(b.invoiceCount())
                        .totalAmount(b.totalAmount())
                        .build())
                .toList();
    }
}
