package com.example.invoiceplatform.service;

import com.example.invoiceplatform.dto.AgingBucketResponse;
import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.dto.MonthlySpendResponse;
import com.example.invoiceplatform.dto.ProductLineResponse;
import com.example.invoiceplatform.dto.SellerSpendResponse;

import java.util.List;

public interface DashboardService {

    DashboardResponse getDashboard();

    List<SellerSpendResponse> getSpendBySeller(int limit);

    List<MonthlySpendResponse> getMonthlyTrend();

    List<AgingBucketResponse> getInvoiceAging();

    List<ProductLineResponse> getProductLines(int limit);
}
