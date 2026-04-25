import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { authApi } from '../../api'
import { useAuthStore } from '../../store/authStore'
import { Button } from '../../components/Button'
import { ApiError } from '../../api/client'

/**
 * Centered login card. Two seeded dev users:
 *   trader1@local.dev / trader1pass
 *   trader2@local.dev / trader2pass
 *
 * Shows credentials inline since this is dev-only — remove for prod obviously.
 */
export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setAuth = useAuthStore((s) => s.setAuth)

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // After login, return to the page the user originally tried to visit (if any).
  // ProtectedRoute below sets `from` on the redirect state.
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const { token, user } = await authApi.login({ email, password })
      setAuth(token, user)
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : 'Login failed. Please try again.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-ink-50 px-6">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <h1 className="text-xl font-semibold tracking-tight text-ink-800">
            Alert Engine
          </h1>
          <p className="text-xs text-ink-400 uppercase tracking-widest mt-1">
            Sign in
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-white border border-ink-200 rounded-lg p-6 space-y-4"
        >
          {error && (
            <div className="p-3 rounded bg-red-50 border border-red-100 text-danger text-xs">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-medium uppercase tracking-wider text-ink-500 mb-1.5">
              Email
            </label>
            <input
              type="email"
              autoComplete="email"
              autoFocus
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 text-sm bg-white border border-ink-200 rounded-md focus:outline-none focus:ring-2 focus:ring-ink-300 focus:border-ink-300"
            />
          </div>

          <div>
            <label className="block text-xs font-medium uppercase tracking-wider text-ink-500 mb-1.5">
              Password
            </label>
            <input
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 text-sm bg-white border border-ink-200 rounded-md focus:outline-none focus:ring-2 focus:ring-ink-300 focus:border-ink-300"
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            disabled={submitting}
            className="w-full"
          >
            {submitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>

        {/* Dev-only credentials hint. Remove in prod. */}
        <div className="mt-6 text-[11px] text-ink-400 text-center space-y-1 font-mono-num">
          <p>Dev users:</p>
          <p>trader1@local.dev · trader1pass</p>
          <p>trader2@local.dev · trader2pass</p>
        </div>
      </div>
    </div>
  )
}