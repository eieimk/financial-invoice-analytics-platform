import * as am5 from '@amcharts/amcharts5'
import * as am5xy from '@amcharts/amcharts5/xy'
import { useAmRoot } from '../../charts/useAmRoot'
import { useChartTheme } from '../../charts/theme'
import { buildSellerSpendData } from '../../charts/configs'
import type { SellerSpend } from '../../types/api'

export function SpendBySellerChart({ rows }: { rows: SellerSpend[] }) {
  const theme = useChartTheme()

  const ref = useAmRoot(theme, (root) => {
    const data = buildSellerSpendData(rows)

    const chart = root.container.children.push(
      am5xy.XYChart.new(root, { panX: false, panY: false, wheelX: 'none', wheelY: 'none' }),
    )

    const yAxis = chart.yAxes.push(
      am5xy.CategoryAxis.new(root, {
        categoryField: 'sellerName',
        renderer: am5xy.AxisRendererY.new(root, { inversed: true, minGridDistance: 20 }),
      }),
    )
    yAxis.get('renderer').grid.template.set('visible', false)
    yAxis.data.setAll(data)

    const xAxis = chart.xAxes.push(
      am5xy.ValueAxis.new(root, {
        min: 0,
        renderer: am5xy.AxisRendererX.new(root, { strokeOpacity: 0 }),
      }),
    )

    const series = chart.series.push(
      am5xy.ColumnSeries.new(root, {
        xAxis,
        yAxis,
        valueXField: 'totalSpend',
        categoryYField: 'sellerName',
        tooltip: am5.Tooltip.new(root, {
          labelText: "{categoryY}: {valueX.formatNumber('$#,###.00')} ({invoiceCount} invoices)",
        }),
      }),
    )
    // Single measure (spend) across categories -> single hue, thin rounded bars
    series.columns.template.setAll({
      fill: am5.color(theme.series1),
      stroke: am5.color(theme.series1),
      height: 22,
      cornerRadiusTR: 4,
      cornerRadiusBR: 4,
    })
    series.data.setAll(data)
    series.appear(600)
  }, [rows])

  return <div ref={ref} className="chart-body" role="img" aria-label="Bar chart of total spend by seller" />
}
