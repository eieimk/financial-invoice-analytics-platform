import * as am5 from "@amcharts/amcharts5";
import * as am5xy from "@amcharts/amcharts5/xy";
import { useAmRoot } from "../../charts/useAmRoot";
import { useChartTheme } from "../../charts/theme";
import { buildAgingData, type AgingDatum } from "../../charts/configs";
import type { AgingBucket } from "../../types/api";

export function InvoiceAgingChart({ rows }: { rows: AgingBucket[] }) {
  const theme = useChartTheme();

  const ref = useAmRoot(
    theme,
    (root) => {
      const data = buildAgingData(rows, theme);

      const chart = root.container.children.push(
        am5xy.XYChart.new(root, {
          panX: false,
          panY: false,
          wheelX: "none",
          wheelY: "none",
        }),
      );

      const xAxis = chart.xAxes.push(
        am5xy.CategoryAxis.new(root, {
          categoryField: "label",
          renderer: am5xy.AxisRendererX.new(root, {
            minGridDistance: 20,
            strokeOpacity: 0,
          }),
        }),
      );
      xAxis.get("renderer").grid.template.set("visible", false);
      xAxis.get("renderer").labels.template.setAll({ fill: am5.color(theme.muted), fontSize: 10 });
      xAxis.data.setAll(data);

      const yAxis = chart.yAxes.push(
        am5xy.ValueAxis.new(root, {
          min: 0,
          renderer: am5xy.AxisRendererY.new(root, { strokeOpacity: 0 }),
        }),
      );
      yAxis.get("renderer").labels.template.setAll({ fill: am5.color(theme.muted), fontSize: 10 });

      const series = chart.series.push(
        am5xy.ColumnSeries.new(root, {
          xAxis,
          yAxis,
          valueYField: "amount",
          categoryXField: "label",
          tooltip: am5.Tooltip.new(root, {
            labelText:
              "{categoryX}: {valueY.formatNumber('$#,###.00')} ({invoiceCount} invoices)",
          }),
        }),
      );
      // Status encoding: each bucket wears its reserved severity color, with the
      // bucket name always on the axis so color never carries meaning alone.
      series.columns.template.setAll({
        maxWidth: 44,
        cornerRadiusTL: 4,
        cornerRadiusTR: 4,
      });
      series.columns.template.adapters.add("fill", (_fill, target) =>
        am5.color((target.dataItem?.dataContext as AgingDatum).color),
      );
      series.columns.template.adapters.add("stroke", (_stroke, target) =>
        am5.color((target.dataItem?.dataContext as AgingDatum).color),
      );
      series.data.setAll(data);
      series.appear(1000, 100);
    },
    [rows],
  );

  return (
    <div
      ref={ref}
      className="chart-body"
      role="img"
      aria-label="Bar chart of invoice amounts by days past due"
    />
  );
}
