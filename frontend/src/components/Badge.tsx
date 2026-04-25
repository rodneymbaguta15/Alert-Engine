import type { ReactNode } from 'react'
import { cn } from '../lib/format'

/**
 * Small pill for status/category display. Tone drives color.
 */
type Tone = 'neutral' | 'success' | 'danger' | 'warning' | 'muted'

const toneClasses: Record<Tone, string> = {
  neutral: 'bg-ink-100 text-ink-700',
  success: 'bg-green-50 text-success',
  danger: 'bg-red-50 text-danger',
  warning: 'bg-amber-50 text-accent',
  muted: 'bg-ink-50 text-ink-400',
}

export function Badge({
  tone = 'neutral',
  children,
  className,
}: {
  tone?: Tone
  children: ReactNode
  className?: string
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium uppercase tracking-wider',
        toneClasses[tone],
        className,
      )}
    >
      {children}
    </span>
  )
}