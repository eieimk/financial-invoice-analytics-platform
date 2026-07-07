import { useLayoutEffect, useRef } from 'react'
import * as am5 from '@amcharts/amcharts5'
import am5themes_Animated from '@amcharts/amcharts5/themes/Animated'
import am5themes_Dark from '@amcharts/amcharts5/themes/Dark'
import type { ChartTheme } from './theme'

/**
 * Owns the am5.Root lifecycle for one chart <div>: create on mount, dispose
 * on unmount/dep change (amCharts leaks canvases otherwise). The builder gets
 * a themed root (Animated always, Dark stacked on top in dark mode) and
 * imperatively assembles the chart — amCharts 5 is imperative by design, so
 * components stay thin and data shaping lives in pure functions (configs.ts).
 */
export function useAmRoot(
  theme: ChartTheme,
  build: (root: am5.Root) => void,
  deps: unknown[],
) {
  const ref = useRef<HTMLDivElement>(null)

  useLayoutEffect(() => {
    if (!ref.current) return
    const root = am5.Root.new(ref.current)
    const themes: am5.Theme[] = [am5themes_Animated.new(root)]
    if (theme.isDark) {
      themes.push(am5themes_Dark.new(root))
    }
    root.setThemes(themes)
    root.numberFormatter.setAll({ numberFormat: '$#,###.00' })
    root._logo?.dispose()
    build(root)
    return () => root.dispose()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [theme, ...deps])

  return ref
}
