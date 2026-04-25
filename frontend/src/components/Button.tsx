import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { cn } from '../lib/format'

/**
 * Single reusable button with variant + size. Keeps visual consistency
 * across the app without reaching for a full UI library.
 *
 * Variants:
 *   - primary: filled accent for main actions ("Create Alert")
 *   - secondary: neutral bordered for safe secondary actions ("Cancel")
 *   - danger: red bordered for destructive actions ("Delete")
 *   - ghost: transparent for low-emphasis actions ("Edit")
 */
type Variant = 'primary' | 'secondary' | 'danger' | 'ghost'
type Size = 'sm' | 'md'

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  children: ReactNode
}

const variantClasses: Record<Variant, string> = {
  primary:
    'bg-ink-800 text-ink-50 border-ink-800 hover:bg-ink-700 hover:border-ink-700',
  secondary:
    'bg-white text-ink-700 border-ink-200 hover:border-ink-300 hover:bg-ink-50',
  danger:
    'bg-white text-danger border-ink-200 hover:border-danger hover:bg-red-50',
  ghost:
    'bg-transparent text-ink-600 border-transparent hover:bg-ink-100 hover:text-ink-800',
}

const sizeClasses: Record<Size, string> = {
  sm: 'text-xs px-2.5 py-1.5',
  md: 'text-sm px-4 py-2',
}

export function Button({
  variant = 'secondary',
  size = 'md',
  className,
  children,
  disabled,
  ...rest
}: Props) {
  return (
    <button
      {...rest}
      disabled={disabled}
      className={cn(
        'inline-flex items-center justify-center rounded-md border font-medium transition-colors',
        'focus:outline-none focus:ring-2 focus:ring-ink-300 focus:ring-offset-1',
        'disabled:opacity-50 disabled:cursor-not-allowed',
        variantClasses[variant],
        sizeClasses[size],
        className,
      )}
    >
      {children}
    </button>
  )
}