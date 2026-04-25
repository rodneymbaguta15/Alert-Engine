import type { Toast as ToastType } from './NotificationProvider'
import { cn } from '../../lib/format'

/**
 * A single toast. Visual hierarchy:
 *   alert  -> accent (amber) left border, neutral background
 *   error  -> danger (red) left border
 *   info   -> plain
 *
 * The pointer-events-auto reactivates clicks (the container is
 * pointer-events-none so toasts don't block the UI behind them).
 */
export function Toast({
  toast,
  onDismiss,
}: {
  toast: ToastType
  onDismiss: () => void
}) {
  const borderClass =
    toast.kind === 'alert'
      ? 'border-l-4 border-l-accent'
      : toast.kind === 'error'
        ? 'border-l-4 border-l-danger'
        : 'border-l-4 border-l-ink-300'

  return (
    <div
      className={cn(
        'pointer-events-auto bg-white border border-ink-200 rounded-md shadow-sm',
        'px-4 py-3 animate-slide-in',
        borderClass,
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="text-sm font-semibold text-ink-800 mb-0.5">
            {toast.title}
          </div>
          {toast.body && (
            <div className="text-xs text-ink-500 font-mono-num">{toast.body}</div>
          )}
        </div>
        <button
          onClick={onDismiss}
          className="text-ink-300 hover:text-ink-600 text-lg leading-none shrink-0"
          aria-label="Dismiss"
        >
          ×
        </button>
      </div>
    </div>
  )
}