import { Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { AlertsPage } from './features/alerts/AlertsPage'
import { HistoryPage } from './features/history/HistoryPage'
import { LoginPage } from './features/auth/LoginPage'
import { ProtectedRoute } from './features/auth/ProtectedRoute'
import { NotificationProvider } from './features/notifications/NotificationProvider'

/**
 * Top-level routing.
 *
 * /login is public; everything else is wrapped in ProtectedRoute which redirects
 * unauthenticated users back to /login (preserving their intended destination).
 *
 * NotificationProvider wraps everything so toasts can fire even on /login if
 * needed (rare, but consistent), and so the WS hook lives at app scope.
 */
function App() {
  return (
    <NotificationProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route index element={<DashboardPage />} />
            <Route path="/alerts" element={<AlertsPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="*" element={<NotFound />} />
          </Route>
        </Route>
      </Routes>
    </NotificationProvider>
  )
}

function NotFound() {
  return (
    <div className="text-center py-20">
      <h2 className="text-xl font-semibold mb-2">Not Found</h2>
      <p className="text-ink-500 text-sm">That page doesn't exist.</p>
    </div>
  )
}

export default App