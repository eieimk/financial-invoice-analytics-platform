import { useDashboard } from "../hooks/useDashboard";
import { useInvoiceUpload } from "../hooks/useInvoiceUpload";
import { useApiData } from "../hooks/useApiData";
import {
  fetchMonthlyTrend,
  fetchProductLines,
  fetchSpendBySeller,
} from "../api/dashboard";
import { StatTile, StatTileSkeleton } from "../components/StatTile";
import { UploadCard } from "../components/UploadCard";
import { ReconciliationScore } from "../components/ReconciliationScore";
import { ReconciliationTable } from "../components/ReconciliationTable";
import { ChartCard } from "../components/ChartCard";
import { SellerLeaderboard } from "../components/SellerLeaderboard";
import { SpendBySellerChart } from "../components/charts/SpendBySellerChart";
import { MonthlyTrendChart } from "../components/charts/MonthlyTrendChart";
import { InvoiceVelocityChart } from "../components/charts/InvoiceVelocityChart";
import { ProductScatterChart } from "../components/charts/ProductScatterChart";
import { ProductWordCloud } from "../components/charts/ProductWordCloud";
import { formatCount, formatMoney } from "../utils/format";

// Module-level so the references are stable across renders (useApiData keys
// its reload effect on the fetcher identity).
const fetchTopSellers = () => fetchSpendBySeller(8);
const fetchTopSellersForCloud = () => fetchSpendBySeller(40);
const fetchLines = () => fetchProductLines(200);

export function DashboardPage() {
  const dashboard = useDashboard();
  const sellerSpend = useApiData(fetchTopSellers);
  const sellerWordCloud = useApiData(fetchTopSellersForCloud);
  const monthlyTrend = useApiData(fetchMonthlyTrend);
  const productLines = useApiData(fetchLines);
  const uploadState = useInvoiceUpload();

  function reloadAll() {
    void dashboard.reload();
    void sellerSpend.reload();
    void sellerWordCloud.reload();
    void monthlyTrend.reload();
    void productLines.reload();
  }

  async function handleUpload(file: File) {
    await uploadState.upload(file);
    // new data may have landed; refresh the headline metrics and charts.
    // The warehouse load task runs async after upload, so refresh once now
    // and once again after it has had a few seconds to complete.
    reloadAll();
    setTimeout(reloadAll, 6000);
  }

  return (
    <main className="page">
      <header className="page__header">
        <div className="page__heading">
          <span className="eyebrow">Seller Insights</span>
          <h1>Financial Invoice Analytics</h1>
          <p className="page__subtitle">Invoice overview from Snowflake</p>
        </div>
      </header>

      <section className="section">
        {dashboard.loading && (
          <div className="stat-grid" aria-hidden="true">
            <StatTileSkeleton label="Total invoices" />
            <StatTileSkeleton label="Total spend" />
            <StatTileSkeleton label="Average invoice" />
            <StatTileSkeleton label="Top vendor" />
          </div>
        )}

        {dashboard.error && (
          <div role="alert" className="banner">
            <span className="banner__message">
              <span aria-hidden="true">⚠</span> Could not load dashboard:{" "}
              {dashboard.error}
            </span>
            <button
              className="banner__retry"
              onClick={() => void dashboard.reload()}
            >
              Retry
            </button>
          </div>
        )}

        {dashboard.data && (
          <div className="stat-grid" data-testid="stat-grid">
            <StatTile
              label="Total invoices"
              value={formatCount(dashboard.data.totalInvoices)}
            />
            <StatTile
              label="Total spend"
              value={formatMoney(dashboard.data.totalRevenue)}
            />
            <StatTile
              label="Average invoice"
              value={formatMoney(dashboard.data.averageInvoiceAmount)}
            />
            <StatTile
              label="Top vendor"
              value={dashboard.data.topVendor ?? "—"}
              detail="by total spend"
            />
          </div>
        )}
      </section>

      <section className="section">
        <h2 className="section__title">Spend analysis</h2>
        <div className="main-grid">
          <ChartCard
            wide
            full
            title="Spend trajectory"
            hint="Total invoice amount per month, from the Snowflake star schema."
            loading={monthlyTrend.loading}
            error={monthlyTrend.error}
            empty={(monthlyTrend.data?.length ?? 0) === 0}
            onRetry={() => void monthlyTrend.reload()}
          >
            <MonthlyTrendChart rows={monthlyTrend.data ?? []} />
          </ChartCard>

          <ChartCard
            wide
            title="Top sellers by spend"
            hint="Top sellers by total invoice amount."
            loading={sellerSpend.loading}
            error={sellerSpend.error}
            empty={(sellerSpend.data?.length ?? 0) === 0}
            onRetry={() => void sellerSpend.reload()}
          >
            <SpendBySellerChart rows={sellerSpend.data ?? []} />
          </ChartCard>

          <ChartCard
            title="Leaderboard"
            hint="Top 5 sellers, ranked."
            loading={sellerSpend.loading}
            error={sellerSpend.error}
            empty={(sellerSpend.data?.length ?? 0) === 0}
            onRetry={() => void sellerSpend.reload()}
          >
            <SellerLeaderboard rows={sellerSpend.data ?? []} />
          </ChartCard>

          <ChartCard
            wide
            full
            title="Invoice count"
            hint="Invoice count per month — throughput, not amount."
            loading={monthlyTrend.loading}
            error={monthlyTrend.error}
            empty={(monthlyTrend.data?.length ?? 0) === 0}
            onRetry={() => void monthlyTrend.reload()}
          >
            <InvoiceVelocityChart rows={monthlyTrend.data ?? []} />
          </ChartCard>
        </div>
      </section>

      <section className="section">
        <h2 className="section__title">Product analysis</h2>
        <div className="chart-grid">
          <ChartCard
            wide
            title="Items over time"
            hint="Every invoice line as a point — by date, sized by price, colored by seller."
            loading={productLines.loading}
            error={productLines.error}
            empty={(productLines.data?.length ?? 0) === 0}
            onRetry={() => void productLines.reload()}
          >
            <ProductScatterChart rows={productLines.data ?? []} />
          </ChartCard>

          <ChartCard
            wide
            title="Top-selling sellers"
            hint="Top 40 sellers by total spend, sized by spend — hover for invoice count."
            loading={sellerWordCloud.loading}
            error={sellerWordCloud.error}
            empty={(sellerWordCloud.data?.length ?? 0) === 0}
            onRetry={() => void sellerWordCloud.reload()}
          >
            <ProductWordCloud rows={sellerWordCloud.data ?? []} />
          </ChartCard>
        </div>
      </section>

      <UploadCard
        uploading={uploadState.uploading}
        error={uploadState.error}
        uploadResult={uploadState.uploadResult}
        onUpload={handleUpload}
      />

      {uploadState.reconciliation && (
        <section className="card">
          <h2>Extraction quality</h2>
          <p className="card__hint">
            Line items summed against each invoice's own stated total.
          </p>
          <ReconciliationScore results={uploadState.reconciliation} />
          <ReconciliationTable results={uploadState.reconciliation} />
        </section>
      )}

      <footer className="page__footer">
        Financial Invoice Analytics · amCharts 5 · Snowflake
      </footer>
    </main>
  );
}
