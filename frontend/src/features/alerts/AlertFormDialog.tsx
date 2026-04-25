import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { tickersApi } from '../../api'
import { useCreateAlert, useUpdateAlert } from './hooks/useAlerts'
import { Dialog } from '../../components/Dialog'
import { Button } from '../../components/Button'
import { ApiError } from '../../api/client'
import type {
  AlertConfigResponse,
  AlertDirection,
  NotificationChannel,
} from '../../types/api'

/**
 * Create/edit form in a modal.
 *
 * One component for both modes to avoid duplication — `initial` being
 * undefined means create mode, otherwise edit mode.
 *
 * Validation happens both:
 *   - Client-side here (cheap, instant feedback)
 *   - Server-side via @Valid (authoritative; bean validation enforces the rules)
 * Server errors come back as ApiError with fieldErrors; we map them onto the
 * relevant inputs so users see exactly which field is wrong.
 */

interface Props {
  open: boolean
  onClose: () => void
  initial?: AlertConfigResponse
}

interface FormState {
  ticker: string
  thresholdPrice: string
  direction: AlertDirection
  cooldownSeconds: number
  channels: NotificationChannel[]
  enabled: boolean
}

const DEFAULT_FORM: FormState = {
  ticker: '',
  thresholdPrice: '',
  direction: 'ABOVE',
  cooldownSeconds: 900,
  channels: ['IN_APP'],
  enabled: true,
}

export function AlertFormDialog({ open, onClose, initial }: Props) {
  const isEdit = initial !== undefined
  const [form, setForm] = useState<FormState>(DEFAULT_FORM)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [topError, setTopError] = useState<string | null>(null)

  const { data: tickers } = useQuery({
    queryKey: ['tickers'],
    queryFn: tickersApi.listAllowed,
    enabled: open,   // only load when dialog actually opens
  })

  const createMut = useCreateAlert()
  const updateMut = useUpdateAlert()
  const submitting = createMut.isPending || updateMut.isPending

  // Reset form when dialog opens, pre-fill in edit mode.
  useEffect(() => {
    if (!open) return
    setFieldErrors({})
    setTopError(null)
    if (initial) {
      setForm({
        ticker: initial.ticker,
        thresholdPrice: initial.thresholdPrice,
        direction: initial.direction,
        cooldownSeconds: initial.cooldownSeconds,
        channels: initial.channels,
        enabled: initial.enabled,
      })
    } else {
      setForm({
        ...DEFAULT_FORM,
        ticker: tickers?.[0] ?? '',   // preselect first allowed ticker
      })
    }
  }, [open, initial, tickers])

  const handleChannelToggle = (channel: NotificationChannel) => {
    setForm((f) => ({
      ...f,
      channels: f.channels.includes(channel)
        ? f.channels.filter((c) => c !== channel)
        : [...f.channels, channel],
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setFieldErrors({})
    setTopError(null)

    // Minimal client-side validation — server is authoritative.
    const errors: Record<string, string> = {}
    if (!form.ticker) errors.ticker = 'Required'
    if (!form.thresholdPrice || Number(form.thresholdPrice) <= 0) {
      errors.thresholdPrice = 'Must be a positive number'
    }
    if (form.channels.length === 0) errors.channels = 'Pick at least one channel'
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    try {
      if (isEdit && initial) {
        await updateMut.mutateAsync({
          id: initial.id,
          payload: {
            thresholdPrice: form.thresholdPrice,
            direction: form.direction,
            cooldownSeconds: form.cooldownSeconds,
            channels: form.channels,
            enabled: form.enabled,
          },
        })
      } else {
        await createMut.mutateAsync({
          ticker: form.ticker,
          thresholdPrice: form.thresholdPrice,
          direction: form.direction,
          cooldownSeconds: form.cooldownSeconds,
          channels: form.channels,
        })
      }
      onClose()
    } catch (err) {
      // Pull ApiError details onto the form.
      if (err instanceof ApiError && err.fieldErrors) {
        const mapped: Record<string, string> = {}
        for (const fe of err.fieldErrors) mapped[fe.field] = fe.message
        setFieldErrors(mapped)
      }
      setTopError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} title={isEdit ? 'Edit Alert' : 'New Alert'}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {topError && (
          <div className="p-3 rounded bg-red-50 border border-red-100 text-danger text-xs">
            {topError}
          </div>
        )}

        {/* Ticker */}
        <Field label="Ticker" error={fieldErrors.ticker}>
          <select
            value={form.ticker}
            onChange={(e) => setForm((f) => ({ ...f, ticker: e.target.value }))}
            disabled={isEdit}   // ticker is immutable on existing alerts
            className={inputClasses}
          >
            {!tickers && <option>Loading…</option>}
            {tickers?.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
          {isEdit && (
            <p className="text-[11px] text-ink-400 mt-1">
              Ticker can't be changed after creation. Delete and re-create instead.
            </p>
          )}
        </Field>

        {/* Threshold + Direction on one row */}
        <div className="grid grid-cols-2 gap-4">
          <Field label="Threshold price" error={fieldErrors.thresholdPrice}>
            <input
              type="number"
              step="0.01"
              min="0"
              value={form.thresholdPrice}
              onChange={(e) =>
                setForm((f) => ({ ...f, thresholdPrice: e.target.value }))
              }
              placeholder="200.00"
              className={inputClasses + ' font-mono-num'}
            />
          </Field>

          <Field label="Direction" error={fieldErrors.direction}>
            <select
              value={form.direction}
              onChange={(e) =>
                setForm((f) => ({ ...f, direction: e.target.value as AlertDirection }))
              }
              className={inputClasses}
            >
              <option value="ABOVE">Above</option>
              <option value="BELOW">Below</option>
            </select>
          </Field>
        </div>

        {/* Cooldown */}
        <Field
          label="Cooldown (seconds)"
          error={fieldErrors.cooldownSeconds}
          hint="Minimum time between re-alerts. Default 900 (15 min)."
        >
          <input
            type="number"
            min={0}
            max={86400}
            value={form.cooldownSeconds}
            onChange={(e) =>
              setForm((f) => ({
                ...f,
                cooldownSeconds: Number(e.target.value) || 0,
              }))
            }
            className={inputClasses + ' font-mono-num'}
          />
        </Field>

        {/* Channels */}
        <Field label="Channels" error={fieldErrors.channels}>
          <div className="flex gap-3">
            <ChannelCheckbox
              label="In-app"
              checked={form.channels.includes('IN_APP')}
              onChange={() => handleChannelToggle('IN_APP')}
            />
            <ChannelCheckbox
              label="Email"
              checked={form.channels.includes('EMAIL')}
              onChange={() => handleChannelToggle('EMAIL')}
            />
          </div>
        </Field>

        {/* Enabled — only visible in edit mode; new alerts always start enabled */}
        {isEdit && (
          <Field label="" error={fieldErrors.enabled}>
            <label className="inline-flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={form.enabled}
                onChange={(e) =>
                  setForm((f) => ({ ...f, enabled: e.target.checked }))
                }
                className="h-4 w-4 rounded border-ink-300"
              />
              <span className="text-sm text-ink-700">Alert enabled</span>
            </label>
          </Field>
        )}

        {/* Actions */}
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? 'Saving…' : isEdit ? 'Save changes' : 'Create alert'}
          </Button>
        </div>
      </form>
    </Dialog>
  )
}

/* ---------- small inline helpers ---------- */

const inputClasses =
  'w-full px-3 py-2 text-sm bg-white border border-ink-200 rounded-md ' +
  'focus:outline-none focus:ring-2 focus:ring-ink-300 focus:border-ink-300'

function Field({
  label,
  error,
  hint,
  children,
}: {
  label: string
  error?: string
  hint?: string
  children: React.ReactNode
}) {
  return (
    <div>
      {label && (
        <label className="block text-xs font-medium uppercase tracking-wider text-ink-500 mb-1.5">
          {label}
        </label>
      )}
      {children}
      {hint && !error && <p className="text-[11px] text-ink-400 mt-1">{hint}</p>}
      {error && <p className="text-[11px] text-danger mt-1">{error}</p>}
    </div>
  )
}

function ChannelCheckbox({
  label,
  checked,
  onChange,
}: {
  label: string
  checked: boolean
  onChange: () => void
}) {
  return (
    <label
      className={
        'flex-1 cursor-pointer select-none px-3 py-2 rounded-md border text-sm transition-colors ' +
        (checked
          ? 'bg-ink-800 text-white border-ink-800'
          : 'bg-white text-ink-700 border-ink-200 hover:border-ink-300')
      }
    >
      <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className="sr-only"
      />
      {label}
    </label>
  )
}