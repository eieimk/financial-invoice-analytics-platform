import { useEffect, useState } from 'react'

/**
 * Chart colors per color scheme. amCharts paints to canvas, so CSS custom
 * properties can't cascade in — the theme is resolved in JS from the same
 * palette the rest of the app uses (see App.css tokens / dataviz reference
 * palette). `categorical` is the fixed slot order for multi-series identity
 * (scatter series by seller) — assigned in order, never cycled or re-ranked.
 * `status` steps are reserved for state (aging severity), never reused as
 * series hues.
 */
export interface ChartTheme {
  isDark: boolean
  ink: string
  muted: string
  grid: string
  series1: string
  categorical: string[]
  status: {
    good: string
    warning: string
    serious: string
    critical: string
    none: string
  }
}

const status = {
  good: '#0ca30c',
  warning: '#fab219',
  serious: '#ec835a',
  critical: '#d03b3b',
  none: '#898781',
}

const light: ChartTheme = {
  isDark: false,
  ink: '#1a1d21',
  muted: '#767d84',
  grid: '#e5e7ea',
  series1: '#2a78d6',
  categorical: ['#2a78d6', '#1baf7a', '#eda100', '#008300', '#4a3aa7', '#e34948', '#e87ba4', '#eb6834'],
  status,
}

const dark: ChartTheme = {
  isDark: true,
  ink: '#e7e9ec',
  muted: '#858c94',
  grid: '#2c3036',
  series1: '#3987e5',
  categorical: ['#3987e5', '#199e70', '#c98500', '#008300', '#9085e9', '#e66767', '#d55181', '#d95926'],
  status,
}

export function useChartTheme(): ChartTheme {
  const query = window.matchMedia?.('(prefers-color-scheme: dark)')
  const [isDark, setIsDark] = useState(query?.matches ?? false)

  useEffect(() => {
    if (!query) return
    const listener = (e: MediaQueryListEvent) => setIsDark(e.matches)
    query.addEventListener('change', listener)
    return () => query.removeEventListener('change', listener)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return isDark ? dark : light
}
