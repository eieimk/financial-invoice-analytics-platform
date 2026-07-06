import type { ApiResponse, ErrorResponse } from '../types/api'

export class ApiError extends Error {
  readonly errorCode: string

  constructor(errorCode: string, message: string) {
    super(message)
    this.errorCode = errorCode
  }
}

// Single place that knows about the envelope contract and error shape;
// endpoint modules just say what they want and get typed data back.
export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init)
  const body = await response.json().catch(() => null)

  if (!response.ok) {
    const error = body as ErrorResponse | null
    throw new ApiError(error?.errorCode ?? 'UNKNOWN', error?.message ?? `Request failed (${response.status})`)
  }

  return (body as ApiResponse<T>).data
}
