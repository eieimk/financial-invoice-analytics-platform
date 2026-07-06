package com.example.invoiceplatform.controller;

import com.example.invoiceplatform.dto.AgingBucketResponse;
import com.example.invoiceplatform.dto.ApiResponse;
import com.example.invoiceplatform.dto.DashboardResponse;
import com.example.invoiceplatform.dto.MonthlySpendResponse;
import com.example.invoiceplatform.dto.ProductLineResponse;
import com.example.invoiceplatform.dto.SellerSpendResponse;
import com.example.invoiceplatform.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "Accounts-payable analytics")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get aggregated invoice metrics")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }

    @Operation(summary = "AP spend grouped by seller, highest first")
    @GetMapping("/spend-by-seller")
    public ResponseEntity<ApiResponse<List<SellerSpendResponse>>> getSpendBySeller(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSpendBySeller(limit)));
    }

    @Operation(summary = "Monthly total spend and invoice count")
    @GetMapping("/monthly-trend")
    public ResponseEntity<ApiResponse<List<MonthlySpendResponse>>> getMonthlyTrend() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyTrend()));
    }

    @Operation(summary = "Invoices bucketed by days past due")
    @GetMapping("/invoice-aging")
    public ResponseEntity<ApiResponse<List<AgingBucketResponse>>> getInvoiceAging() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInvoiceAging()));
    }

    @Operation(summary = "Individual invoice line items with date and seller, for scatter/word-cloud analysis")
    @GetMapping("/product-lines")
    public ResponseEntity<ApiResponse<List<ProductLineResponse>>> getProductLines(
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getProductLines(limit)));
    }
}
