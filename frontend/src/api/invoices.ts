import { request } from './client'
import type { InvoiceReconciliationResult, UploadResultResponse } from '../types/api'

export function uploadInvoiceFile(file: File): Promise<UploadResultResponse> {
  const form = new FormData()
  form.append('file', file)
  return request<UploadResultResponse>('/api/v1/invoices/upload', { method: 'POST', body: form })
}

export function parseInvoiceFile(file: File): Promise<InvoiceReconciliationResult[]> {
  const form = new FormData()
  form.append('file', file)
  return request<InvoiceReconciliationResult[]>('/api/v1/invoices/parse', { method: 'POST', body: form })
}
