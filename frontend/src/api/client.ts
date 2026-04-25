import axios, { AxiosError } from 'axios'
import type { ApiErrorResponse } from '../types/api'
import { useAuthStore } from '../store/authStore'

/**
 * Shared axios instance.
 *
 * Two interceptors:
 *   - Request: attach Bearer token if we have one
 *   - Response: on 401 from a protected endpoint, clear auth state and
 *     redirect to /login so the user re-authenticates instead of staring at
 *     a broken page.
 */
export const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors?: { field: string; message: string }[]
  readonly original: AxiosError

  constructor(
    message: string,
    status: number,
    fieldErrors: { field: string; message: string }[] | undefined,
    original: AxiosError,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
    this.original = original
  }
}

// Attach JWT if present.
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    const status = error.response?.status ?? 0
    const backendError = error.response?.data
    const requestUrl = error.config?.url ?? ''

    // 401 on a protected endpoint → token expired/invalid. Clear and bounce.
    // Don't redirect on 401 from /auth/login itself — that's just bad credentials,
    // the LoginPage shows the error inline.
    if (status === 401 && !requestUrl.includes('/auth/login')) {
      useAuthStore.getState().clear()
      // Hard redirect rather than react-router navigate — interceptor isn't a
      // component and doesn't have access to the router. This guarantees the
      // app remounts cleanly with no stale state.
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    const message =
      backendError?.message ??
      (status === 0 ? 'Cannot reach the server' : `Request failed (${status})`)

    throw new ApiError(message, status, backendError?.fieldErrors, error)
  },
)