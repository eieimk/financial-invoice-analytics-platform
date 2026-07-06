import { describe, expect, it } from 'vitest'
import {
  buildAgingData,
  buildMonthlyTrendData,
  buildScatterSeries,
  buildSellerSpendData,
  buildSellerWordData,
  orderAgingBuckets,
} from './configs'
import type { ChartTheme } from './theme'
import type { AgingBucket, MonthlySpend, ProductLine, SellerSpend } from '../types/api'

const theme: ChartTheme = {
  isDark: false,
  ink: '#000',
  muted: '#777',
  grid: '#eee',
  series1: '#2a78d6',
  categorical: ['#2a78d6', '#1baf7a', '#eda100'],
  status: { good: '#0ca30c', warning: '#fab219', serious: '#ec835a', critical: '#d03b3b', none: '#898781' },
}

describe('orderAgingBuckets', () => {
  it('orders buckets by severity and drops missing ones', () => {
    const rows: AgingBucket[] = [
      { bucket: '60_PLUS_DAYS', invoiceCount: 1, totalAmount: 100 },
      { bucket: 'CURRENT', invoiceCount: 2, totalAmount: 200 },
    ]
    expect(orderAgingBuckets(rows).map((r) => r.bucket)).toEqual(['CURRENT', '60_PLUS_DAYS'])
  })
})

describe('buildAgingData', () => {
  it('assigns the reserved status color per bucket severity', () => {
    const rows: AgingBucket[] = [
      { bucket: 'CURRENT', invoiceCount: 2, totalAmount: 200 },
      { bucket: '1-30_DAYS', invoiceCount: 1, totalAmount: 50 },
      { bucket: '60_PLUS_DAYS', invoiceCount: 1, totalAmount: 100 },
    ]
    const data = buildAgingData(rows, theme)
    expect(data.map((d) => d.label)).toEqual(['Current', '1–30 days', '60+ days'])
    expect(data.map((d) => d.color)).toEqual(['#0ca30c', '#fab219', '#d03b3b'])
  })
})

describe('buildSellerSpendData', () => {
  it('sorts sellers by spend, highest first', () => {
    const rows: SellerSpend[] = [
      { sellerName: 'Nguyen-Roach', invoiceCount: 1, totalSpend: 232.95, avgInvoiceAmount: 232.95 },
      { sellerName: 'Cascade Industrial', invoiceCount: 1, totalSpend: 480, avgInvoiceAmount: 480 },
    ]
    const data = buildSellerSpendData(rows)
    expect(data.map((d) => d.sellerName)).toEqual(['Cascade Industrial', 'Nguyen-Roach'])
  })
})

describe('buildMonthlyTrendData', () => {
  it('converts yyyy-MM to first-of-month epoch timestamps', () => {
    const rows: MonthlySpend[] = [{ month: '2021-03', totalSpend: 680.5, invoiceCount: 2 }]
    const data = buildMonthlyTrendData(rows)
    expect(data[0].date).toBe(Date.UTC(2021, 2, 1))
    expect(data[0].totalSpend).toBe(680.5)
  })
})

const lines: ProductLine[] = [
  {
    description: 'Vintage Crystal Red Wine Glasses',
    quantity: 1,
    totalPrice: 39,
    invoiceDate: '2021-02-23',
    sellerName: 'Nguyen-Roach',
  },
  {
    description: 'Ikea Stainless Steel Wine Rack',
    quantity: 4,
    totalPrice: 110,
    invoiceDate: '2021-02-23',
    sellerName: 'Nguyen-Roach',
  },
  {
    description: 'Hydraulic pump assembly',
    quantity: 1,
    totalPrice: 300,
    invoiceDate: '2021-03-20',
    sellerName: 'Cascade Industrial',
  },
  {
    description: 'No date line',
    quantity: 1,
    totalPrice: 10,
    invoiceDate: null,
    sellerName: 'Cascade Industrial',
  },
]

describe('buildScatterSeries', () => {
  it('groups points by seller with stable alphabetical color slots, skipping dateless rows', () => {
    const series = buildScatterSeries(lines, theme)
    expect(series.map((s) => s.sellerName)).toEqual(['Cascade Industrial', 'Nguyen-Roach'])
    expect(series[0].color).toBe('#2a78d6')
    expect(series[1].color).toBe('#1baf7a')
    expect(series[0].points).toHaveLength(1) // dateless row dropped
    expect(series[1].points).toHaveLength(2)
    expect(series[1].points[0].quantity).toBe(1)
  })
})

describe('buildSellerWordData', () => {
  const sellers: SellerSpend[] = [
    { sellerName: 'Cascade Industrial', invoiceCount: 2, totalSpend: 480, avgInvoiceAmount: 240 },
    { sellerName: 'Nguyen-Roach', invoiceCount: 1, totalSpend: 232.95, avgInvoiceAmount: 232.95 },
    { sellerName: 'Blue Ridge Supplies', invoiceCount: 1, totalSpend: 200.5, avgInvoiceAmount: 200.5 },
  ]

  it('sorts sellers by spend, highest first, and assigns categorical colors in order', () => {
    const words = buildSellerWordData(sellers, theme)
    expect(words.map((w) => w.tag)).toEqual(['Cascade Industrial', 'Nguyen-Roach', 'Blue Ridge Supplies'])
    expect(words.map((w) => w.color)).toEqual(['#2a78d6', '#1baf7a', '#eda100'])
    expect(words[0].weight).toBe(480)
    expect(words[0].invoiceCount).toBe(2)
  })

  it('caps to maxWords', () => {
    const words = buildSellerWordData(sellers, theme, 2)
    expect(words).toHaveLength(2)
  })

  it('cycles the categorical palette past its slot count', () => {
    const many = Array.from({ length: 5 }, (_, i) => ({
      sellerName: `Seller ${i}`,
      invoiceCount: 1,
      totalSpend: 100 - i,
      avgInvoiceAmount: 100 - i,
    }))
    const words = buildSellerWordData(many, theme, 5)
    expect(words[3].color).toBe(theme.categorical[0])
    expect(words[4].color).toBe(theme.categorical[1])
  })
})
