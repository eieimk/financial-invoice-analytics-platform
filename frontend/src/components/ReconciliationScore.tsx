import type { CSSProperties } from 'react'
import { computeReconciliationScore } from '../utils/reconciliation'
import type { InvoiceReconciliationResult } from '../types/api'

/** Headline extraction-quality score for the just-uploaded file, ahead of the row-by-row table. */
export function ReconciliationScore({ results }: { results: InvoiceReconciliationResult[] }) {
  const score = computeReconciliationScore(results)

  return (
    <div className="score-card">
      <div className="score-card__ring" style={{ '--pct': score.pct } as CSSProperties}>
        <span className="score-card__pct">{score.pct}%</span>
      </div>
      <div className="score-card__info">
        <span className="score-card__label">Extraction match rate</span>
        <span className="score-card__detail">
          {score.reconciled} of {score.total} invoice{score.total === 1 ? '' : 's'} reconciled cleanly
        </span>
      </div>
    </div>
  )
}
