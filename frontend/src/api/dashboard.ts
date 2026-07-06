import { request } from './client'
import type { AgingBucket, DashboardResponse, MonthlySpend, ProductLine, SellerSpend } from '../types/api'

export function fetchDashboard(): Promise<DashboardResponse> {
  return request<DashboardResponse>('/api/v1/dashboard')
}

export function fetchSpendBySeller(limit = 10): Promise<SellerSpend[]> {
  return request<SellerSpend[]>(`/api/v1/dashboard/spend-by-seller?limit=${limit}`)
}

export function fetchMonthlyTrend(): Promise<MonthlySpend[]> {
  return request<MonthlySpend[]>('/api/v1/dashboard/monthly-trend')
}

export function fetchInvoiceAging(): Promise<AgingBucket[]> {
  return request<AgingBucket[]>('/api/v1/dashboard/invoice-aging')
}

export function fetchProductLines(limit = 200): Promise<ProductLine[]> {
  return request<ProductLine[]>(`/api/v1/dashboard/product-lines?limit=${limit}`)
}
