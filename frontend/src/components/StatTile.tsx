interface StatTileProps {
  label: string
  value: string
  detail?: string
  icon?: string
}

export function StatTile({ label, value, detail, icon }: StatTileProps) {
  return (
    <div className="stat-tile">
      <div className="stat-tile__top">
        {icon && <span className="stat-tile__icon" aria-hidden="true">{icon}</span>}
        <div className="stat-tile__label">{label}</div>
      </div>
      <div className="stat-tile__value">{value}</div>
      {detail && <div className="stat-tile__detail">{detail}</div>}
    </div>
  )
}

/** Placeholder tile matching StatTile's footprint, shown while data loads. */
export function StatTileSkeleton({ label }: { label: string }) {
  return (
    <div className="stat-tile stat-tile--skeleton" aria-hidden="true">
      <div className="stat-tile__label">{label}</div>
      <div className="stat-tile__value">{label}</div>
    </div>
  )
}
