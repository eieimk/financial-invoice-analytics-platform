import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ReconciliationTable } from './ReconciliationTable'
import type { InvoiceReconciliationResult } from '../types/api'

const clean: InvoiceReconciliationResult = {
  rowNumber: 1,
  invoiceNumber: '84652373',
  sellerName: 'Nguyen-Roach',
  clientName: 'Clark-Foster',
  lineItemSum: 232.95,
  statedTotal: 232.95,
  discrepancy: false,
  difference: 0,
}

const broken: InvoiceReconciliationResult = {
  ...clean,
  rowNumber: 3,
  invoiceNumber: '10000002',
  lineItemSum: 450,
  statedTotal: 480,
  discrepancy: true,
  difference: 30,
}

describe('ReconciliationTable', () => {
  it('shows reconciled status when line items sum to the stated total', () => {
    render(<ReconciliationTable results={[clean]} />)
    expect(screen.getByText('84652373')).toBeInTheDocument()
    expect(screen.getByText(/Reconciled/)).toBeInTheDocument()
  })

  it('flags discrepancies as needing review', () => {
    render(<ReconciliationTable results={[broken]} />)
    expect(screen.getByText(/Needs review/)).toBeInTheDocument()
    expect(screen.getByText('$30.00')).toBeInTheDocument()
  })

  it('renders an empty state when no rows parsed', () => {
    render(<ReconciliationTable results={[]} />)
    expect(screen.getByText(/No invoices found/)).toBeInTheDocument()
  })
})
