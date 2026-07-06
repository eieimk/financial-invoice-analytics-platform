import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import { useAmRoot } from '../../charts/useAmRoot'
import { useChartTheme } from '../../charts/theme'
import { buildMonthlyTrendData } from '../../charts/configs'
import type { MonthlySpend } from '../../types/api'

export function MonthlyTrendChart({ rows }: { rows: MonthlySpend[] }) {
  const theme = useChartTheme()

  const ref = useAmRoot(theme, (root) => {
    const data = buildMonthlyTrendData(rows)

    const chart = root.container.children.push(
      am5xy.XYChart.new(root, { panX: false, panY: false, wheelX: 'none', wheelY: 'none' }),
    )

    const xAxis = chart.xAxes.push(
      am5xy.DateAxis.new(root, {
        baseInterval: { timeUnit: 'month', count: 1 },
        renderer: am5xy.AxisRendererX.new(root, { minGridDistance: 60, strokeOpacity: 0 }),
      }),
    )
    xAxis.get('renderer').grid.template.set('visible', false)

    const yAxis = chart.yAxes.push(
      am5xy.ValueAxis.new(root, {
        min: 0,
        renderer: am5xy.AxisRendererY.new(root, { strokeOpacity: 0 }),
      }),
    )

    const series = chart.series.push(
      am5xy.LineSeries.new(root, {
        xAxis,
        yAxis,
        valueXField: 'date',
        valueYField: 'totalSpend',
        stroke: am5.color(theme.series1),
        fill: am5.color(theme.series1),
        tooltip: am5.Tooltip.new(root, {
          labelText: "{valueX.formatDate('yyyy-MM')}: {valueY.formatNumber('$#,###.00')} ({invoiceCount} invoices)",
        }),
      }),
    )
    series.strokes.template.set('strokeWidth', 2)
    series.fills.template.setAll({ visible: true, fillOpacity: 0.12 })
    series.bullets.push(() =>
      am5.Bullet.new(root, {
        sprite: am5.Circle.new(root, { radius: 4, fill: am5.color(theme.series1) }),
      }),
    )
    series.data.setAll(data)

    chart.set('cursor', am5xy.XYCursor.new(root, { behavior: 'none', xAxis }))
    series.appear(600)
  }, [rows])

  return <div ref={ref} className="chart-body" role="img" aria-label="Line chart of monthly total spend" />
}
