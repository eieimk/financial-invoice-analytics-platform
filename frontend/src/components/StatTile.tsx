interface StatTileProps {
  label: string
  value: string
  detail?: string
  icon?: string
  /** Period-over-period % change, when derivable from real trend data — omit rather than fabricate. */
  deltaPct?: number | null
}

/** Long values (e.g. a long vendor name) need a smaller font to avoid overflowing/wrapping the tile. */
function valueSizeClass(value: string): string {
  if (value.length > 20) return 'stat-tile__value stat-tile__value--xs'
  if (value.length > 12) return 'stat-tile__value stat-tile__value--sm'
  return 'stat-tile__value'
}

export function StatTile({ label, value, detail, icon, deltaPct }: StatTileProps) {
  const progress = Math.min(Math.abs(deltaPct ?? 0) * 4, 100)

  return (
    <div className="stat-tile">
      <div className="stat-tile__top">
        {icon && <span className="stat-tile__icon" aria-hidden="true">{icon}</span>}
        <div className="stat-tile__label">{label}</div>
      </div>
      <div className={valueSizeClass(value)}>{value}</div>
      {detail && <div className="stat-tile__detail">{detail}</div>}
      {deltaPct != null && (
        <div className={deltaPct >= 0 ? 'stat-tile__delta stat-tile__delta--up' : 'stat-tile__delta stat-tile__delta--down'}>
          {deltaPct >= 0 ? '▲' : '▼'} {Math.abs(deltaPct).toFixed(1)}%
        </div>
      )}
      <div className="stat-tile__bar">
        <div className="stat-tile__bar-fill" style={{ width: `${deltaPct != null ? progress : 66}%` }} />
      </div>
    </div>
  )
}

/** Placeholder tile matching StatTile's footprint, shown while data loads. */
export function StatTileSkeleton({ label }: { label: string }) {
  return (
    <div className="stat-tile stat-tile--skeleton" aria-hidden="true">
      <div className="stat-tile__label">{label}</div>
      <div className={valueSizeClass(label)}>{label}</div>
    </div>
  )
}
