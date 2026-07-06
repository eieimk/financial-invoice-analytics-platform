import { useCallback, useState } from 'react'
import { parseInvoiceFile, uploadInvoiceFile } from '../api/invoices'
import type { InvoiceReconciliationResult, UploadResultResponse } from '../types/api'

interface UploadState {
  uploading: boolean
  error: string | null
  uploadResult: UploadResultResponse | null
  reconciliation: InvoiceReconciliationResult[] | null
}

const initial: UploadState = { uploading: false, error: null, uploadResult: null, reconciliation: null }

/**
 * Uploads the CSV to the S3 landing zone, then runs the parse+reconcile
 * endpoint on the same file so the user sees extraction quality immediately.
 */
export function useInvoiceUpload() {
  const [state, setState] = useState<UploadState>(initial)

  const upload = useCallback(async (file: File) => {
    setState({ ...initial, uploading: true })
    try {
      const uploadResult = await uploadInvoiceFile(file)
      const reconciliation = await parseInvoiceFile(file)
      setState({ uploading: false, error: null, uploadResult, reconciliation })
    } catch (e) {
      setState({ ...initial, error: e instanceof Error ? e.message : 'Upload failed' })
    }
  }, [])

  return { ...state, upload }
}
