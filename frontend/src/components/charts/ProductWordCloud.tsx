import * as am5 from '@amcharts/amcharts5'
import * as am5wc from '@amcharts/amcharts5/wc'
import { useAmRoot } from '../../charts/useAmRoot'
import { useChartTheme } from '../../charts/theme'
import { buildSellerWordData, type SellerWordDatum } from '../../charts/configs'
import type { SellerSpend } from '../../types/api'

/**
 * Top 20 sellers by spend, one word per seller. Multicolor by design here:
 * each word is a distinct entity (a seller), not a magnitude ramp, so color
 * carries identity - the fixed categorical palette is assigned per word.
 */
export function ProductWordCloud({ rows }: { rows: SellerSpend[] }) {
  const theme = useChartTheme()

  const ref = useAmRoot(theme, (root) => {
    const data = buildSellerWordData(rows, theme)

    const series = root.container.children.push(
      am5wc.WordCloud.new(root, {
        categoryField: 'tag',
        valueField: 'weight',
        calculateAggregates: true,
        maxFontSize: am5.percent(14),
        minFontSize: am5.percent(4),
        randomness: 0.3,
      }),
    )
    series.labels.template.setAll({
      fontFamily: "system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif",
      tooltipText: "{tag}: {weight.formatNumber('$#,###.00')} total · {invoiceCount} invoices",
      cursorOverStyle: 'default',
    })
    // dataContext is undefined for labels amCharts creates internally (e.g.
    // during layout/measurement passes), so fall back to the default series
    // color rather than assuming every label backs a real data row.
    series.labels.template.adapters.add('fill', (fill, target) => {
      const color = (target.dataItem?.dataContext as SellerWordDatum | undefined)?.color
      return color ? am5.color(color) : fill
    })
    series.data.setAll(data)
    series.appear(600)
  }, [rows])

  return (
    <div
      ref={ref}
      className="chart-body"
      role="img"
      aria-label="Word cloud of top 20 sellers sized by total spend"
    />
  )
}
