import type {
  AgingBucket,
  AgingBucketName,
  MonthlySpend,
  ProductLine,
  SellerSpend,
} from "../types/api";
import type { ChartTheme } from "./theme";

// Pure data -> chart-datum shaping, kept out of the components so it can be
// unit tested without a canvas. The amCharts components consume these shapes.

const AGING_ORDER: AgingBucketName[] = [
  "CURRENT",
  "1-30_DAYS",
  "31-60_DAYS",
  "60_PLUS_DAYS",
  "NO_DUE_DATE",
];

export const AGING_LABELS: Record<AgingBucketName, string> = {
  CURRENT: "Current",
  "1-30_DAYS": "1–30 days",
  "31-60_DAYS": "31–60 days",
  "60_PLUS_DAYS": "60+ days",
  NO_DUE_DATE: "No due date",
};

/** Orders buckets by severity and drops buckets the backend didn't return. */
export function orderAgingBuckets(rows: AgingBucket[]): AgingBucket[] {
  const byName = new Map(rows.map((r) => [r.bucket, r]));
  return AGING_ORDER.flatMap((name) => byName.get(name) ?? []);
}

export interface AgingDatum {
  label: string;
  amount: number;
  invoiceCount: number;
  color: string;
}

/**
 * Aging is a *status* encoding (how overdue), not series identity — each
 * bucket takes the reserved status step for its severity, with the bucket
 * name always on the axis so color never carries the meaning alone.
 */
export function buildAgingData(
  rows: AgingBucket[],
  theme: ChartTheme,
): AgingDatum[] {
  const statusByBucket: Record<AgingBucketName, string> = {
    CURRENT: theme.status.good,
    "1-30_DAYS": theme.status.warning,
    "31-60_DAYS": theme.status.serious,
    "60_PLUS_DAYS": theme.status.critical,
    NO_DUE_DATE: theme.status.none,
  };
  return orderAgingBuckets(rows).map((r) => ({
    label: AGING_LABELS[r.bucket],
    amount: r.totalAmount,
    invoiceCount: r.invoiceCount,
    color: statusByBucket[r.bucket],
  }));
}

export interface SellerSpendDatum {
  sellerName: string;
  totalSpend: number;
  invoiceCount: number;
}

/** Highest spend first; the horizontal bar chart renders top-down. */
export function buildSellerSpendData(rows: SellerSpend[]): SellerSpendDatum[] {
  return [...rows]
    .sort((a, b) => b.totalSpend - a.totalSpend)
    .map((r) => ({
      sellerName: r.sellerName,
      totalSpend: r.totalSpend,
      invoiceCount: r.invoiceCount,
    }));
}

export interface TrendDatum {
  date: number; // epoch ms, first of month
  totalSpend: number;
  invoiceCount: number;
}

export function buildMonthlyTrendData(rows: MonthlySpend[]): TrendDatum[] {
  return rows.map((r) => {
    const [year, month] = r.month.split("-").map(Number);
    return {
      date: Date.UTC(year, month - 1, 1),
      totalSpend: r.totalSpend,
      invoiceCount: r.invoiceCount,
    };
  });
}

/** Same shape as the spend trend, just the count field driving the bar — invoice throughput per month. */
export function buildInvoiceVelocityData(rows: MonthlySpend[]): TrendDatum[] {
  return buildMonthlyTrendData(rows);
}

/**
 * Trailing-window trend + period-over-period delta, computed client-side
 * from the same monthly series the trend chart uses — real numbers, not a
 * fabricated KPI, so the "7D/30D/90D/YTD" pills change what's actually shown.
 */
export type TrendWindow = "1M" | "3M" | "6M" | "YTD";

export function filterTrendWindow(
  data: TrendDatum[],
  window: TrendWindow,
): TrendDatum[] {
  if (data.length === 0) return data;
  if (window === "YTD") {
    const currentYear = new Date(data[data.length - 1].date).getUTCFullYear();
    return data.filter(
      (d) => new Date(d.date).getUTCFullYear() === currentYear,
    );
  }
  const months = { "1M": 1, "3M": 3, "6M": 6 }[window];
  return data.slice(-months);
}

export interface PeriodDelta {
  pct: number | null;
  positive: boolean;
}

/** % change of the last point in the window vs. the one before it — null when there's nothing to compare against. */
export function computeDelta(
  data: TrendDatum[],
  field: "totalSpend" | "invoiceCount",
): PeriodDelta {
  if (data.length < 2) return { pct: null, positive: true };
  const prev = data[data.length - 2][field];
  const curr = data[data.length - 1][field];
  if (prev === 0) return { pct: null, positive: true };
  const pct = ((curr - prev) / prev) * 100;
  return { pct, positive: pct >= 0 };
}

export interface SellerMixDatum {
  sellerName: string;
  totalSpend: number;
  color: string;
}

/** Top 4 sellers by spend as named slices, everything else rolled into "Other" — keeps the donut legible past 4 sellers. */
export function buildSellerMixData(
  rows: SellerSpend[],
  theme: ChartTheme,
  topN = 4,
): SellerMixDatum[] {
  const sorted = [...rows].sort((a, b) => b.totalSpend - a.totalSpend);
  const top = sorted.slice(0, topN);
  const rest = sorted.slice(topN);
  const otherTotal = rest.reduce((sum, r) => sum + r.totalSpend, 0);

  const slices = top.map((r, i) => ({
    sellerName: r.sellerName,
    totalSpend: r.totalSpend,
    color: theme.categorical[i % theme.categorical.length],
  }));
  if (otherTotal > 0) {
    slices.push({
      sellerName: "Other",
      totalSpend: otherTotal,
      color: theme.muted,
    });
  }
  return slices;
}

export interface ScatterSeries {
  sellerName: string;
  color: string;
  points: Array<{
    date: number;
    price: number;
    quantity: number;
    description: string;
  }>;
}

/** Truncates to 15 characters (+ ellipsis) — keeps the tooltip line item description from blowing out the box. */
function truncateDescription(description: string, maxLength = 15): string {
  return description.length > maxLength
    ? `${description.slice(0, maxLength)}…`
    : description;
}

/**
 * One scatter series per seller (identity — categorical slots in fixed
 * order), points at (invoice date, line price), bullet radius from quantity.
 * Capped to the top sellers by point count, rest folded into "Other" — an
 * unbounded per-seller legend gets long and unreadable fast, and past the
 * 8-slot categorical palette colors would cycle/repeat anyway.
 */
export function buildScatterSeries(
  rows: ProductLine[],
  theme: ChartTheme,
  maxSeries = 7,
): ScatterSeries[] {
  const bySeller = new Map<string, ScatterSeries["points"]>();
  for (const row of rows) {
    if (!row.invoiceDate) continue;
    const seller = row.sellerName ?? "Unknown";
    const points = bySeller.get(seller) ?? [];
    points.push({
      date: new Date(row.invoiceDate).getTime(),
      price: row.totalPrice,
      quantity: row.quantity,
      description: truncateDescription(row.description),
    });
    bySeller.set(seller, points);
  }

  const bySellerSorted = [...bySeller.entries()].sort(
    ([, a], [, b]) => b.length - a.length,
  );
  const top = bySellerSorted.slice(0, maxSeries);
  const rest = bySellerSorted.slice(maxSeries);

  const series = top
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([sellerName, points], i) => ({
      sellerName,
      color: theme.categorical[i % theme.categorical.length],
      points,
    }));

  if (rest.length > 0) {
    series.push({
      sellerName: "Other",
      color: theme.muted,
      points: rest.flatMap(([, points]) => points),
    });
  }

  return series;
}

export interface SellerWordDatum {
  tag: string; // seller name
  weight: number; // total spend - drives word size
  invoiceCount: number;
  color: string;
}

/**
 * Top sellers by spend, one word per seller. Each seller is a distinct
 * category (not a magnitude ramp), so unlike the single-hue bar/line charts
 * this assigns the fixed categorical palette per word, cycling past the
 * 8-slot floor if there are more than 8 sellers - acceptable here because a
 * word cloud carries its own direct label per mark, so there's no legend to
 * desync (the "never cycle" rule is about legend-bound series identity).
 */
export function buildSellerWordData(
  rows: SellerSpend[],
  theme: ChartTheme,
  maxWords = 20,
): SellerWordDatum[] {
  return [...rows]
    .sort((a, b) => b.totalSpend - a.totalSpend)
    .slice(0, maxWords)
    .map((r, i) => ({
      tag: r.sellerName,
      weight: r.totalSpend,
      invoiceCount: r.invoiceCount,
      color: theme.categorical[i % theme.categorical.length],
    }));
}
