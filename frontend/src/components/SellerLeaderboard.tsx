import type { SellerSpend } from '../types/api'
import { formatMoney, formatCount } from '../utils/format'

/** Top 5 sellers by spend, same data as the bar chart — a scannable, ranked companion view. */
export function SellerLeaderboard({ rows }: { rows: SellerSpend[] }) {
  const top5 = [...rows].sort((a, b) => b.totalSpend - a.totalSpend).slice(0, 5)

  if (top5.length === 0) {
    return (
      <div className="empty-state">
        <span className="empty-state__icon" aria-hidden="true">📭</span>
        <p>No data yet — upload some invoices first.</p>
      </div>
    )
  }

  return (
    <ol className="leaderboard">
      {top5.map((r, i) => (
        <li key={r.sellerName} className="leaderboard__row">
          <span className="leaderboard__badge">{i + 1}</span>
          <span className="leaderboard__info">
            <span className="leaderboard__name">{r.sellerName}</span>
            <span className="leaderboard__subtitle">
              {formatCount(r.invoiceCount)} invoice{r.invoiceCount === 1 ? '' : 's'} · avg {formatMoney(r.avgInvoiceAmount)}
            </span>
          </span>
          <span className="leaderboard__total">{formatMoney(r.totalSpend)}</span>
        </li>
      ))}
    </ol>
  )
}
