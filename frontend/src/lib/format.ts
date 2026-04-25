import { format, formatDistanceToNow, parseISO } from 'date-fns'

/**
 * Small collection of formatters. Keeping them here (not inline in components)
 * ensures consistent display across the app — prices always render the same way,
 * timestamps always use the same format.
 */

/** "$123.4500" -> "$123.45" (trim to 2 decimals for display). */
export function formatPrice(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === '') return '—'
  const num = typeof value === 'string' ? Number(value) : value
  if (!Number.isFinite(num)) return '—'
  return num.toLocaleString('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

/** "2026-04-23T19:14:21Z" -> "Apr 23, 2026 · 3:14 PM" (local time). */
export function formatTimestamp(iso: string | null | undefined): string {
  if (!iso) return '—'
  try {
    return format(parseISO(iso), "MMM d, yyyy '·' h:mm a")
  } catch {
    return iso
  }
}

/** "2026-04-23T19:14:21Z" -> "3 minutes ago". */
export function formatRelative(iso: string | null | undefined): string {
  if (!iso) return '—'
  try {
    return formatDistanceToNow(parseISO(iso), { addSuffix: true })
  } catch {
    return iso
  }
}

/** "900" -> "15m" (human-readable duration). */
export function formatCooldown(seconds: number): string {
  if (seconds < 60) return `${seconds}s`
  if (seconds < 3600) return `${Math.round(seconds / 60)}m`
  return `${Math.round(seconds / 3600)}h`
}

/** tailwind-merge-lite: join class names, filter falsy. Used by conditional styles. */
export function cn(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ')
}