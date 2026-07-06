import { useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import type { UploadResultResponse } from '../types/api'

interface UploadCardProps {
  uploading: boolean
  error: string | null
  uploadResult: UploadResultResponse | null
  onUpload: (file: File) => void
}

export function UploadCard({ uploading, error, uploadResult, onUpload }: UploadCardProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [dragActive, setDragActive] = useState(false)

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) {
      onUpload(file)
    }
    // allow re-selecting the same file
    event.target.value = ''
  }

  function handleDrop(event: DragEvent<HTMLLabelElement>) {
    event.preventDefault()
    setDragActive(false)
    const file = event.dataTransfer.files?.[0]
    if (file && !uploading) {
      onUpload(file)
    }
  }

  function handleDragOver(event: DragEvent<HTMLLabelElement>) {
    event.preventDefault()
    if (!uploading) {
      setDragActive(true)
    }
  }

  return (
    <section className="card">
      <h2>Upload invoice CSV</h2>
      <p className="card__hint">
        CSV with a single JSON_DATA column (one invoice extraction per row) — lands in S3, then parsed and reconciled.
      </p>

      <label
        className={[
          'dropzone',
          dragActive && 'dropzone--active',
          uploading && 'dropzone--disabled',
        ].filter(Boolean).join(' ')}
        onDragOver={handleDragOver}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
      >
        <span className="dropzone__icon" aria-hidden="true">📄</span>
        <span className="dropzone__title">{uploading ? 'Uploading…' : 'Click or drag a CSV here'}</span>
        <span className="dropzone__hint">.csv files only</span>
        <input
          ref={inputRef}
          type="file"
          accept=".csv"
          onChange={handleChange}
          disabled={uploading}
          data-testid="file-input"
        />
      </label>

      {uploading && (
        <p role="status" className="upload-status">Uploading…</p>
      )}
      {error && (
        <p role="alert" className="upload-status error-text">⚠ {error}</p>
      )}
      {uploadResult && (
        <p role="status" className="upload-status success-text">
          ✓ {uploadResult.fileName} uploaded to s3://{uploadResult.bucket}/{uploadResult.s3Key}
          {uploadResult.rowsLoadedToWarehouse != null && uploadResult.rowsLoadedToWarehouse > 0 && (
            <> — {uploadResult.rowsLoadedToWarehouse} row{uploadResult.rowsLoadedToWarehouse === 1 ? '' : 's'} loaded to Snowflake
            {uploadResult.warehouseRefreshTriggered ? ', charts refreshing shortly' : ''}</>
          )}
        </p>
      )}
    </section>
  )
}
