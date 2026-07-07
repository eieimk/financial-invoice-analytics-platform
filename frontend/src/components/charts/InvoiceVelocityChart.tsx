import * as am5 from "@amcharts/amcharts5";
import * as am5xy from "@amcharts/amcharts5/xy";
import { useAmRoot } from "../../charts/useAmRoot";
import { useChartTheme } from "../../charts/theme";
import { buildInvoiceVelocityData } from "../../charts/configs";
import type { MonthlySpend } from "../../types/api";

/** Invoice throughput per month — same series as the spend trend, count field instead of amount. */
export function InvoiceVelocityChart({ rows }: { rows: MonthlySpend[] }) {
  const theme = useChartTheme();

  const ref = useAmRoot(
    theme,
    (root) => {
      const data = buildInvoiceVelocityData(rows);

      const chart = root.container.children.push(
        am5xy.XYChart.new(root, {
          panX: false,
          panY: false,
          wheelX: "none",
          wheelY: "none",
        }),
      );

      const xAxis = chart.xAxes.push(
        am5xy.DateAxis.new(root, {
          baseInterval: { timeUnit: "month", count: 1 },
          renderer: am5xy.AxisRendererX.new(root, {
            minGridDistance: 50,
            strokeOpacity: 0,
          }),
        }),
      );
      xAxis.get("renderer").grid.template.set("visible", false);
      xAxis
        .get("renderer")
        .labels.template.setAll({ fill: am5.color(theme.muted), fontSize: 10 });

      const yAxis = chart.yAxes.push(
        am5xy.ValueAxis.new(root, {
          min: 0,
          // Override the root's currency numberFormat ($#,###.00) — this
          // axis is an invoice count, not money.
          numberFormat: "#",
          renderer: am5xy.AxisRendererY.new(root, { strokeOpacity: 0 }),
        }),
      );
      yAxis
        .get("renderer")
        .labels.template.setAll({ fill: am5.color(theme.muted), fontSize: 10 });

      const gradient = am5.LinearGradient.new(root, {
        stops: [
          { color: am5.color(theme.series2) },
          { color: am5.color(theme.series1) },
        ],
        rotation: 90,
      });

      const series = chart.series.push(
        am5xy.ColumnSeries.new(root, {
          xAxis,
          yAxis,
          valueXField: "date",
          valueYField: "invoiceCount",
          tooltip: am5.Tooltip.new(root, {
            labelText:
              "{valueX.formatDate('yyyy-MM')}: [bold]{valueY.formatNumber('0')}[/] invoices",
          }),
        }),
      );
      series.columns.template.setAll({
        width: am5.percent(60),
        cornerRadiusTL: 6,
        cornerRadiusTR: 6,
        fillGradient: gradient,
        strokeGradient: gradient,
      });
      series.data.setAll(data);

      chart.set(
        "cursor",
        am5xy.XYCursor.new(root, { behavior: "none", xAxis }),
      );
      series.appear(1000, 100);
    },
    [rows],
  );

  return (
    <div
      ref={ref}
      className="chart-body"
      role="img"
      aria-label="Column chart of invoice count per month"
    />
  );
}
