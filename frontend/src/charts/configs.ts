import type { AgingBucket, AgingBucketName, MonthlySpend, ProductLine, SellerSpend } from '../types/api'
import type { ChartTheme } from './theme'

// Pure data -> chart-datum shaping, kept out of the components so it can be
// unit tested without a canvas. The amCharts components consume these shapes.

const AGING_ORDER: AgingBucketName[] = ['CURRENT', '1-30_DAYS', '31-60_DAYS', '60_PLUS_DAYS', 'NO_DUE_DATE']

export const AGING_LABELS: Record<AgingBucketName, string> = {
  CURRENT: 'Current',
  '1-30_DAYS': '1–30 days',
  '31-60_DAYS': '31–60 days',
  '60_PLUS_DAYS': '60+ days',
  NO_DUE_DATE: 'No due date',
}

/** Orders buckets by severity and drops buckets the backend didn't return. */
export function orderAgingBuckets(rows: AgingBucket[]): AgingBucket[] {
  const byName = new Map(rows.map((r) => [r.bucket, r]))
  return AGING_ORDER.flatMap((name) => byName.get(name) ?? [])
}

export interface AgingDatum {
  label: string
  amount: number
  invoiceCount: number
  color: string
}

/**
 * Aging is a *status* encoding (how overdue), not series identity — each
 * bucket takes the reserved status step for its severity, with the bucket
 * name always on the axis so color never carries the meaning alone.
 */
export function buildAgingData(rows: AgingBucket[], theme: ChartTheme): AgingDatum[] {
  const statusByBucket: Record<AgingBucketName, string> = {
    CURRENT: theme.status.good,
    '1-30_DAYS': theme.status.warning,
    '31-60_DAYS': theme.status.serious,
    '60_PLUS_DAYS': theme.status.critical,
    NO_DUE_DATE: theme.status.none,
  }
  return orderAgingBuckets(rows).map((r) => ({
    label: AGING_LABELS[r.bucket],
    amount: r.totalAmount,
    invoiceCount: r.invoiceCount,
    color: statusByBucket[r.bucket],
  }))
}

export interface SellerSpendDatum {
  sellerName: string
  totalSpend: number
  invoiceCount: number
}

/** Highest spend first; the horizontal bar chart renders top-down. */
export function buildSellerSpendData(rows: SellerSpend[]): SellerSpendDatum[] {
  return [...rows]
    .sort((a, b) => b.totalSpend - a.totalSpend)
    .map((r) => ({ sellerName: r.sellerName, totalSpend: r.totalSpend, invoiceCount: r.invoiceCount }))
}

export interface TrendDatum {
  date: number // epoch ms, first of month
  totalSpend: number
  invoiceCount: number
}

export function buildMonthlyTrendData(rows: MonthlySpend[]): TrendDatum[] {
  return rows.map((r) => {
    const [year, month] = r.month.split('-').map(Number)
    return {
      date: Date.UTC(year, month - 1, 1),
      totalSpend: r.totalSpend,
      invoiceCount: r.invoiceCount,
    }
  })
}

export interface ScatterSeries {
  sellerName: string
  color: string
  points: Array<{ date: number; price: number; quantity: number; description: string }>
}

/**
 * One scatter series per seller (identity — categorical slots in fixed
 * order), points at (invoice date, line price), bullet radius from quantity.
 */
export function buildScatterSeries(rows: ProductLine[], theme: ChartTheme): ScatterSeries[] {
  const bySeller = new Map<string, ScatterSeries['points']>()
  for (const row of rows) {
    if (!row.invoiceDate) continue
    const seller = row.sellerName ?? 'Unknown'
    const points = bySeller.get(seller) ?? []
    points.push({
      date: new Date(row.invoiceDate).getTime(),
      price: row.totalPrice,
      quantity: row.quantity,
      description: row.description,
    })
    bySeller.set(seller, points)
  }
  return [...bySeller.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([sellerName, points], i) => ({
      sellerName,
      color: theme.categorical[i % theme.categorical.length],
      points,
    }))
}

export interface SellerWordDatum {
  tag: string // seller name
  weight: number // total spend - drives word size
  invoiceCount: number
  color: string
}

/**
 * Top sellers by spend, one word per seller. Each seller is a distinct
 * category (not a magnitude ramp), so unlike the single-hue bar/line charts
 * this assigns the fixed categorical palette per word, cycling past the
 * 8-slot floor if there are more than 8 sellers - acceptable here because a
 * word cloud carries its own direct label per mark, so there's no legend to
 * desync (the "never cycle" rule is about legend-bound series identity).
 */
export function buildSellerWordData(rows: SellerSpend[], theme: ChartTheme, maxWords = 20): SellerWordDatum[] {
  return [...rows]
    .sort((a, b) => b.totalSpend - a.totalSpend)
    .slice(0, maxWords)
    .map((r, i) => ({
      tag: r.sellerName,
      weight: r.totalSpend,
      invoiceCount: r.invoiceCount,
      color: theme.categorical[i % theme.categorical.length],
    }))
}
