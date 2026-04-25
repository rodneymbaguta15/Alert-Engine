import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

/**
 * Route guard. If unauthenticated, redirect to /login while preserving the
 * intended destination in router state — LoginPage reads it and returns
 * the user there after successful sign in.
 *
 * `replace` so the user can't hit Back to bypass.
 */
export function ProtectedRoute() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated())
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}