// Hand-mirrored from the backend dto/ package. In a production system these
// would be generated from the OpenAPI spec (springdoc already exposes it at
// /v3/api-docs) to eliminate drift; hand-mirroring keeps the demo dependency-free.

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface ErrorResponse {
  success: false
  errorCode: string
  message: string
  path: string
  timestamp: string
}

export interface DashboardResponse {
  totalInvoices: number
  totalRevenue: number
  averageInvoiceAmount: number
  topVendor: string | null
}

export interface UploadResultResponse {
  fileName: string
  s3Key: string
  bucket: string
  sizeBytes: number
  uploadedAt: string
  rowsLoadedToWarehouse?: number
  warehouseRefreshTriggered?: boolean
}

export interface SellerSpend {
  sellerName: string
  invoiceCount: number
  totalSpend: number
  avgInvoiceAmount: number
}

export interface MonthlySpend {
  month: string // ISO yyyy-MM
  totalSpend: number
  invoiceCount: number
}

export type AgingBucketName = 'NO_DUE_DATE' | 'CURRENT' | '1-30_DAYS' | '31-60_DAYS' | '60_PLUS_DAYS'

export interface AgingBucket {
  bucket: AgingBucketName
  invoiceCount: number
  totalAmount: number
}

export interface ProductLine {
  description: string
  quantity: number
  totalPrice: number
  invoiceDate: string | null // ISO yyyy-MM-dd
  sellerName: string | null
}

export interface InvoiceReconciliationResult {
  rowNumber: number
  invoiceNumber: string | null
  sellerName: string | null
  clientName: string | null
  lineItemSum: number | null
  statedTotal: number | null
  discrepancy: boolean
  difference: number | null
}
