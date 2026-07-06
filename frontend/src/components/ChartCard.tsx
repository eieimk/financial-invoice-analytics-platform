import type { ReactNode } from 'react'

interface ChartCardProps {
  title: string
  hint: string
  loading: boolean
  error: string | null
  empty: boolean
  onRetry: () => void
  children: ReactNode
  wide?: boolean
}

/** Shared card chrome + loading/error/empty states for the dashboard charts. */
export function ChartCard({ title, hint, loading, error, empty, onRetry, children, wide }: ChartCardProps) {
  return (
    <section className={wide ? 'card chart-card chart-card--wide' : 'card chart-card'}>
      <h2>{title}</h2>
      <p className="card__hint">{hint}</p>
      {loading && <div className="chart-skeleton" aria-hidden="true" />}
      {error && (
        <div role="alert" className="banner">
          <span className="banner__message">
            <span aria-hidden="true">⚠</span> {error}
          </span>
          <button className="banner__retry" onClick={onRetry}>Retry</button>
        </div>
      )}
      {!loading && !error && empty && (
        <div className="empty-state">
          <span className="empty-state__icon" aria-hidden="true">📭</span>
          <p>No data yet — upload some invoices first.</p>
        </div>
      )}
      {/* children bring their own .chart-body div (charts size to it) */}
      {!loading && !error && !empty && children}
    </section>
  )
}
