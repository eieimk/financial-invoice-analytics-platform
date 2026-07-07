import type { InvoiceReconciliationResult } from '../types/api'

export interface ReconciliationScore {
  total: number
  reconciled: number
  pct: number // 0-100, rounded
}

/** Share of rows where the line-item sum matched the invoice's own stated total — the extraction-quality headline. */
export function computeReconciliationScore(results: InvoiceReconciliationResult[]): ReconciliationScore {
  const total = results.length
  const reconciled = results.filter((r) => !r.discrepancy).length
  const pct = total === 0 ? 0 : Math.round((reconciled / total) * 100)
  return { total, reconciled, pct }
}
