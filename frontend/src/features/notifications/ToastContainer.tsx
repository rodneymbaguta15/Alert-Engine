import { useNotifications } from './NotificationProvider'
import { Toast } from './Toast'

/**
 * Fixed-position container that stacks toasts top-right of the viewport.
 * Renders nothing if no toasts — keeps the DOM clean.
 */
export function ToastContainer() {
  const { toasts, dismiss } = useNotifications()

  if (toasts.length === 0) return null

  return (
    <div
      className="fixed top-4 right-4 z-50 flex flex-col gap-2 w-full max-w-sm pointer-events-none"
      aria-live="polite"
      aria-atomic="false"
    >
      {toasts.map((t) => (
        <Toast key={t.id} toast={t} onDismiss={() => dismiss(t.id)} />
      ))}
    </div>
  )
}