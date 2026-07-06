import type { InvoiceReconciliationResult } from '../types/api'
import { formatMoney } from '../utils/format'

interface ReconciliationTableProps {
  results: InvoiceReconciliationResult[]
}

export function ReconciliationTable({ results }: ReconciliationTableProps) {
  if (results.length === 0) {
    return (
      <div className="empty-state">
        <span className="empty-state__icon" aria-hidden="true">🗂️</span>
        <p>No invoices found in the uploaded file.</p>
      </div>
    )
  }

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Row</th>
            <th>Invoice #</th>
            <th>Seller</th>
            <th>Client</th>
            <th className="num">Line-item sum</th>
            <th className="num">Stated total</th>
            <th className="num">Difference</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {results.map((r) => (
            <tr key={`${r.rowNumber}-${r.invoiceNumber}`}>
              <td>{r.rowNumber}</td>
              <td>{r.invoiceNumber ?? '—'}</td>
              <td>{r.sellerName ?? '—'}</td>
              <td>{r.clientName ?? '—'}</td>
              <td className="num">{formatMoney(r.lineItemSum)}</td>
              <td className="num">{formatMoney(r.statedTotal)}</td>
              <td className="num">{formatMoney(r.difference)}</td>
              <td>
                {r.discrepancy ? (
                  <span className="status status--warning">⚠ Needs review</span>
                ) : (
                  <span className="status status--good">✓ Reconciled</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
