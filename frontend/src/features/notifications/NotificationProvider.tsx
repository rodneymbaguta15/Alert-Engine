import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../../store/authStore'
import { useWebSocket, type WsStatus } from '../../hooks/useWebSocket'
import { ToastContainer } from './ToastContainer'
import type { InAppAlert } from '../../types/api'

export interface Toast {
  id: number
  kind: 'alert' | 'info' | 'error'
  title: string
  body?: string
}

interface NotificationContextValue {
  toasts: Toast[]
  dismiss: (id: number) => void
  pushToast: (toast: Omit<Toast, 'id'>) => void
  wsStatus: WsStatus
}

const NotificationContext = createContext<NotificationContextValue | null>(null)

const TOAST_AUTO_DISMISS_MS = 6_000

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const nextIdRef = useRef(1)
  const queryClient = useQueryClient()

  // Read both userId and token from auth store. WebSocket only connects when
  // both are present (i.e., user is logged in).
  const userId = useAuthStore((s) => s.user?.id)
  const token = useAuthStore((s) => s.token)

  const pushToast = useCallback((toast: Omit<Toast, 'id'>) => {
    const id = nextIdRef.current++
    setToasts((prev) => [...prev, { ...toast, id }])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, TOAST_AUTO_DISMISS_MS)
  }, [])

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const handleAlert = useCallback(
    (payload: unknown) => {
      const alert = payload as InAppAlert
      const directionWord = alert.direction === 'ABOVE' ? 'rose above' : 'fell below'
      pushToast({
        kind: 'alert',
        title: `${alert.ticker} ${directionWord} $${Number(alert.thresholdPrice).toFixed(2)}`,
        body: `Current price: $${Number(alert.currentPrice).toFixed(2)}`,
      })
      queryClient.invalidateQueries({ queryKey: ['history'] })
    },
    [pushToast, queryClient],
  )

  const { status: wsStatus } = useWebSocket({
    userId,
    token,
    onMessage: handleAlert,
    // The hook itself bails if userId/token are missing, but being explicit here
    // documents the intent and enables future toggling (e.g., a "do not disturb" flag).
    enabled: userId !== undefined && token !== null,
  })

  // Disconnect notification — only after we'd connected at least once.
  const wasConnectedRef = useRef(false)
  useEffect(() => {
    if (wsStatus === 'connected') {
      wasConnectedRef.current = true
    } else if (wsStatus === 'disconnected' && wasConnectedRef.current) {
      pushToast({
        kind: 'error',
        title: 'Realtime connection lost',
        body: 'Trying to reconnect…',
      })
      wasConnectedRef.current = false
    }
  }, [wsStatus, pushToast])

  return (
    <NotificationContext.Provider value={{ toasts, dismiss, pushToast, wsStatus }}>
      {children}
      <ToastContainer />
    </NotificationContext.Provider>
  )
}

export function useNotifications() {
  const ctx = useContext(NotificationContext)
  if (!ctx) {
    throw new Error('useNotifications must be used inside NotificationProvider')
  }
  return ctx
}