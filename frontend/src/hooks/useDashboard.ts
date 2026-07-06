import { useCallback, useEffect, useState } from 'react'
import { fetchDashboard } from '../api/dashboard'
import type { DashboardResponse } from '../types/api'

interface DashboardState {
  data: DashboardResponse | null
  loading: boolean
  error: string | null
}

export function useDashboard() {
  const [state, setState] = useState<DashboardState>({ data: null, loading: true, error: null })

  const reload = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: null }))
    try {
      const data = await fetchDashboard()
      setState({ data, loading: false, error: null })
    } catch (e) {
      setState({ data: null, loading: false, error: e instanceof Error ? e.message : 'Failed to load dashboard' })
    }
  }, [])

  useEffect(() => {
    void reload()
  }, [reload])

  return { ...state, reload }
}
