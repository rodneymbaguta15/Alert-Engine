import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../../store/authStore'
import { cn } from '../../lib/format'
import { useNotifications } from '../../features/notifications/NotificationProvider'

export function Layout() {
  const user = useAuthStore((s) => s.user)
  const clear = useAuthStore((s) => s.clear)
  const { wsStatus } = useNotifications()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const handleLogout = () => {
    // Clear React Query cache so the next user doesn't briefly see stale data
    // from the previous user during the redirect.
    queryClient.clear()
    clear()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen flex flex-col bg-ink-50 text-ink-800">
      <header className="border-b border-ink-200 bg-white">
        <div className="mx-auto max-w-6xl px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-8">
            <div className="flex items-baseline gap-2">
              <span className="text-lg font-semibold tracking-tight">Alert Engine</span>
              <span className="text-[10px] uppercase tracking-widest text-ink-400 font-medium">
                v0.1
              </span>
            </div>

            <nav className="flex items-center gap-1">
              <NavItem to="/">Dashboard</NavItem>
              <NavItem to="/alerts">Alerts</NavItem>
              <NavItem to="/history">History</NavItem>
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <ConnectionIndicator status={wsStatus} />
            <div className="text-sm text-ink-500">
              <span className="text-ink-400">Signed in as</span>{' '}
              <span className="font-medium text-ink-700">
                {user?.displayName ?? user?.email ?? '…'}
              </span>
            </div>
            <button
              onClick={handleLogout}
              className="text-xs text-ink-400 hover:text-ink-700 transition-colors"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <div className="mx-auto max-w-6xl px-6 py-10">
          <Outlet />
        </div>
      </main>
    </div>
  )
}

function NavItem({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        cn(
          'px-3 py-1.5 text-sm rounded transition-colors',
          isActive
            ? 'text-ink-900 bg-ink-100 font-medium'
            : 'text-ink-500 hover:text-ink-800 hover:bg-ink-100/50',
        )
      }
    >
      {children}
    </NavLink>
  )
}

function ConnectionIndicator({ status }: { status: 'connecting' | 'connected' | 'disconnected' }) {
  const config = {
    connecting: { color: 'bg-amber-400', label: 'Connecting', pulse: true },
    connected: { color: 'bg-success', label: 'Live', pulse: false },
    disconnected: { color: 'bg-ink-300', label: 'Offline', pulse: false },
  }[status]

  return (
    <div className="flex items-center gap-1.5 text-xs text-ink-500">
      <span className="relative flex h-2 w-2">
        {config.pulse && (
          <span
            className={cn(
              'absolute inline-flex h-full w-full rounded-full opacity-75 animate-ping',
              config.color,
            )}
          />
        )}
        <span className={cn('relative inline-flex rounded-full h-2 w-2', config.color)} />
      </span>
      <span className="uppercase tracking-wider text-[10px] font-medium">{config.label}</span>
    </div>
  )
}