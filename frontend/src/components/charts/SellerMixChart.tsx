import * as am5 from '@amcharts/amcharts5'
import * as am5percent from '@amcharts/amcharts5/percent'
import { useAmRoot } from '../../charts/useAmRoot'
import { useChartTheme } from '../../charts/theme'
import { buildSellerMixData } from '../../charts/configs'
import type { SellerSpend } from '../../types/api'

/** Top-4 sellers' share of total spend, donut, everything else rolled into "Other". */
export function SellerMixChart({ rows }: { rows: SellerSpend[] }) {
  const theme = useChartTheme()

  const ref = useAmRoot(theme, (root) => {
    const data = buildSellerMixData(rows, theme)

    const chart = root.container.children.push(
      am5percent.PieChart.new(root, { innerRadius: am5.percent(62) }),
    )

    const series = chart.series.push(
      am5percent.PieSeries.new(root, {
        categoryField: 'sellerName',
        valueField: 'totalSpend',
        alignLabels: false,
      }),
    )
    series.slices.template.setAll({
      strokeWidth: 2,
      stroke: am5.color(0x0b0d17),
      cornerRadius: 4,
      templateField: 'sliceSettings',
    })
    series.slices.template.adapters.add('fill', (_fill, target) =>
      am5.color((target.dataItem?.dataContext as { color: string }).color),
    )
    series.slices.template.set(
      'tooltipText',
      "{category}: {value.formatNumber('$#,###.00')} ({valuePercentTotal.formatNumber('0.0')}%)",
    )
    series.labels.template.setAll({ fontSize: 11, fill: am5.color(theme.muted) })
    series.ticks.template.set('stroke', am5.color(theme.grid))

    series.data.setAll(data)
    series.appear(1000, 100)
  }, [rows])

  return <div ref={ref} className="chart-body" role="img" aria-label="Donut chart of top sellers' share of total spend" />
}
