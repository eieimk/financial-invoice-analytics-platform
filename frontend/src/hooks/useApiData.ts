import { useCallback, useEffect, useState } from 'react'

interface ApiDataState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

/**
 * Generic read-only fetch hook: same loading/error/reload contract as
 * useDashboard, shared by the chart endpoints so each chart doesn't
 * reimplement state handling.
 */
export function useApiData<T>(fetcher: () => Promise<T>) {
  const [state, setState] = useState<ApiDataState<T>>({ data: null, loading: true, error: null })

  const reload = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: null }))
    try {
      const data = await fetcher()
      setState({ data, loading: false, error: null })
    } catch (e) {
      setState({ data: null, loading: false, error: e instanceof Error ? e.message : 'Failed to load' })
    }
  }, [fetcher])

  useEffect(() => {
    void reload()
  }, [reload])

  return { ...state, reload }
}
