import * as am5 from "@amcharts/amcharts5";
import * as am5xy from "@amcharts/amcharts5/xy";
import { useAmRoot } from "../../charts/useAmRoot";
import { useChartTheme } from "../../charts/theme";
import { buildScatterSeries } from "../../charts/configs";
import type { ProductLine } from "../../types/api";

/**
 * Each point is one invoice line item: x = invoice date, y = line total,
 * bullet radius scaled by quantity. One series per seller (identity ->
 * categorical slots in fixed order); ≥2 series so a legend is present.
 */
export function ProductScatterChart({ rows }: { rows: ProductLine[] }) {
  const theme = useChartTheme();

  const ref = useAmRoot(
    theme,
    (root) => {
      const seriesData = buildScatterSeries(rows, theme);

      const chart = root.container.children.push(
        am5xy.XYChart.new(root, {
          panX: false,
          panY: false,
          wheelX: "none",
          wheelY: "none",
          layout: root.verticalLayout,
          paddingRight: 24,
        }),
      );

      const xAxis = chart.xAxes.push(
        am5xy.DateAxis.new(root, {
          baseInterval: { timeUnit: "day", count: 1 },
          renderer: am5xy.AxisRendererX.new(root, {
            minGridDistance: 60,
            strokeOpacity: 0,
          }),
        }),
      );
      xAxis.get("renderer").grid.template.set("visible", false);

      const yAxis = chart.yAxes.push(
        am5xy.ValueAxis.new(root, {
          min: 0,
          renderer: am5xy.AxisRendererY.new(root, { strokeOpacity: 0 }),
        }),
      );

      for (const s of seriesData) {
        const series = chart.series.push(
          am5xy.LineSeries.new(root, {
            name: s.sellerName,
            xAxis,
            yAxis,
            valueXField: "date",
            valueYField: "price",
            stroke: am5.color(s.color),
            fill: am5.color(s.color),
          }),
        );
        // scatter: no connecting line, just bullets sized by quantity
        series.strokes.template.set("strokeOpacity", 0);
        series.bullets.push((_root, _series, dataItem) => {
          const quantity = (dataItem.dataContext as { quantity: number })
            .quantity;
          const circle = am5.Circle.new(root, {
            radius: Math.min(4 + quantity * 2, 14),
            fill: am5.color(s.color),
            fillOpacity: 0.8,
            stroke: am5.color(theme.isDark ? "#131518" : "#ffffff"),
            strokeWidth: 2,
            tooltipText:
              "{description}\n[bold]{valueY.formatNumber('$#,###.00')}[/] · qty {quantity} · {valueX.formatDate('yyyy-MM-dd')}",
          });
          return am5.Bullet.new(root, { sprite: circle });
        });
        series.data.setAll(s.points);
        series.appear(600);
      }

      const legend = chart.children.push(
        am5.Legend.new(root, { centerX: am5.p50, x: am5.p50, marginTop: 12 }),
      );
      legend.data.setAll(chart.series.values);
    },
    [rows],
  );

  return (
    <div
      ref={ref}
      className="chart-body chart-body--tall"
      role="img"
      aria-label="Scatter plot of invoice line items over time, grouped by seller, point size by quantity"
    />
  );
}
