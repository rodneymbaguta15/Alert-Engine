import { useEffect, type ReactNode } from 'react'

/**
 * Minimal modal dialog — backdrop + centered card.
 *
 * Closes on:
 *   - Escape key
 *   - Clicking the backdrop
 *   - Parent calling onClose (e.g., after save)
 *
 * Uses HTML <dialog> would give us focus trap for free, but its
 * ::backdrop styling support varies, so we roll our own. Good enough for
 * a form modal at this scale. For production consider @radix-ui/react-dialog.
 */
interface Props {
  open: boolean
  onClose: () => void
  title: string
  children: ReactNode
}

export function Dialog({ open, onClose, title, children }: Props) {
  // Esc to close
  useEffect(() => {
    if (!open) return
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [open, onClose])

  // Prevent body scroll while open
  useEffect(() => {
    if (!open) return
    const prev = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = prev
    }
  }, [open])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      onMouseDown={(e) => {
        // Backdrop click closes — only if the click target IS the backdrop,
        // not a descendant inside the dialog content.
        if (e.target === e.currentTarget) onClose()
      }}
    >
      <div className="absolute inset-0 bg-ink-900/40" />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative w-full max-w-md bg-white border border-ink-200 rounded-lg shadow-lg"
      >
        <div className="px-6 py-4 border-b border-ink-200">
          <h2 className="text-base font-semibold tracking-tight text-ink-800">{title}</h2>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  )
}