import { render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DashboardPage } from './DashboardPage'
import type { AgingBucket, DashboardResponse, MonthlySpend, ProductLine, SellerSpend } from '../types/api'

// jsdom has no canvas/ResizeObserver so amCharts can't boot; the data shaping
// is covered by the pure builder tests (charts/configs.test.ts), so stub the
// chart components themselves here.
vi.mock('../components/charts/SpendBySellerChart', () => ({
  SpendBySellerChart: () => <div data-testid="chart-seller" />,
}))
vi.mock('../components/charts/MonthlyTrendChart', () => ({
  MonthlyTrendChart: () => <div data-testid="chart-trend" />,
}))
vi.mock('../components/charts/InvoiceAgingChart', () => ({
  InvoiceAgingChart: () => <div data-testid="chart-aging" />,
}))
vi.mock('../components/charts/ProductScatterChart', () => ({
  ProductScatterChart: () => <div data-testid="chart-scatter" />,
}))
vi.mock('../components/charts/ProductWordCloud', () => ({
  ProductWordCloud: () => <div data-testid="chart-wordcloud" />,
}))

function envelope<T>(data: T) {
  return { success: true, message: 'OK', data, timestamp: new Date().toISOString() }
}

const metrics: DashboardResponse = {
  totalInvoices: 3,
  totalRevenue: 913.45,
  averageInvoiceAmount: 304.48,
  topVendor: 'Cascade Industrial',
}

const sellers: SellerSpend[] = [
  { sellerName: 'Cascade Industrial', invoiceCount: 1, totalSpend: 480, avgInvoiceAmount: 480 },
]

const trend: MonthlySpend[] = [{ month: '2021-03', totalSpend: 680.5, invoiceCount: 2 }]

const aging: AgingBucket[] = [{ bucket: '60_PLUS_DAYS', invoiceCount: 3, totalAmount: 913.45 }]

const productLines: ProductLine[] = [
  { description: 'Hydraulic pump assembly', quantity: 1, totalPrice: 300, invoiceDate: '2021-03-20', sellerName: 'Cascade Industrial' },
]

function stubFetchByUrl(overrides: Record<string, () => Response> = {}) {
  vi.stubGlobal('fetch', vi.fn().mockImplementation((input: RequestInfo | URL) => {
    const url = String(input)
    for (const [fragment, make] of Object.entries(overrides)) {
      if (url.includes(fragment)) return Promise.resolve(make())
    }
    if (url.includes('spend-by-seller')) return Promise.resolve(json(envelope(sellers)))
    if (url.includes('monthly-trend')) return Promise.resolve(json(envelope(trend)))
    if (url.includes('invoice-aging')) return Promise.resolve(json(envelope(aging)))
    if (url.includes('product-lines')) return Promise.resolve(json(envelope(productLines)))
    return Promise.resolve(json(envelope(metrics)))
  }))
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('DashboardPage', () => {
  it('renders stat tiles from the dashboard endpoint', async () => {
    stubFetchByUrl()

    render(<DashboardPage />)

    await waitFor(() => expect(screen.getByTestId('stat-grid')).toBeInTheDocument())
    expect(screen.getByText('Cascade Industrial')).toBeInTheDocument()
    expect(screen.getByText('$913.45')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
  })

  it('renders the five chart cards once their data loads', async () => {
    stubFetchByUrl()

    render(<DashboardPage />)

    await waitFor(() => expect(screen.getByTestId('chart-trend')).toBeInTheDocument())
    expect(screen.getByTestId('chart-seller')).toBeInTheDocument()
    expect(screen.getByTestId('chart-aging')).toBeInTheDocument()
    expect(screen.getByTestId('chart-scatter')).toBeInTheDocument()
    expect(screen.getByTestId('chart-wordcloud')).toBeInTheDocument()
    expect(screen.getByText('Monthly spend')).toBeInTheDocument()
    expect(screen.getByText('Line items over time')).toBeInTheDocument()
    expect(screen.getByText('Top-selling sellers')).toBeInTheDocument()
  })

  it('shows an error state with retry when the metrics endpoint fails', async () => {
    stubFetchByUrl({
      '/api/v1/dashboard': () => json({ success: false, errorCode: 'INTERNAL_ERROR', message: 'boom' }, 500),
    })

    render(<DashboardPage />)

    await waitFor(() => expect(screen.getAllByRole('alert')[0]).toHaveTextContent('boom'))
    expect(screen.getAllByRole('button', { name: /retry/i }).length).toBeGreaterThan(0)
  })

  it('shows a per-chart empty state when a chart endpoint returns no rows', async () => {
    stubFetchByUrl({
      'invoice-aging': () => json(envelope([])),
    })

    render(<DashboardPage />)

    await waitFor(() => expect(screen.getByText(/No data yet/)).toBeInTheDocument())
  })
})
