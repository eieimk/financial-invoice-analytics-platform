/**
 * Chart colors. amCharts paints to canvas, so CSS custom properties can't
 * cascade in — the palette is resolved in JS from the same tokens App.css
 * uses. This dashboard commits to one dark, boutique look (not a
 * system-preference toggle) — see App.css for the rationale.
 * `categorical` is the fixed slot order for multi-series identity (scatter
 * series by seller) — assigned in order, never cycled or re-ranked.
 * `status` steps are reserved for state (aging severity), never reused as
 * series hues.
 */
export interface ChartTheme {
  isDark: boolean
  ink: string
  muted: string
  grid: string
  series1: string
  series2: string
  categorical: string[]
  status: {
    good: string
    warning: string
    serious: string
    critical: string
    none: string
  }
}

const theme: ChartTheme = {
  isDark: true,
  ink: '#e2e8f0',
  muted: '#7d8290',
  grid: 'rgba(255, 255, 255, 0.06)',
  series1: '#7c5cff',
  series2: '#22d3ee',
  categorical: ['#7c5cff', '#22d3ee', '#f472b6', '#f59e0b', '#34d399', '#9085e9', '#e66767', '#d95926'],
  status: {
    good: '#34d399',
    warning: '#f59e0b',
    serious: '#f472b6',
    critical: '#f43f5e',
    none: '#64748b',
  },
}

/** Always the same dark palette — this dashboard's look is a fixed identity, not a system-preference toggle. */
export function useChartTheme(): ChartTheme {
  return theme
}
