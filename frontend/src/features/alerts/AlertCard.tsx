import { useState } from 'react'
import type { AlertConfigResponse } from '../../types/api'
import { formatPrice, formatCooldown, formatRelative } from '../../lib/format'
import { useDeleteAlert } from './hooks/useAlerts'
import { Button } from '../../components/Button'
import { Badge } from '../../components/Badge'

/**
 * Single alert card. Shows the rule at a glance + edit/delete actions.
 *
 * Deletion uses a simple inline confirm state — two clicks to delete, which is
 * good enough without a separate confirmation modal. The second click goes
 * straight through to the API.
 */
interface Props {
  alert: AlertConfigResponse
  onEdit: () => void
}

export function AlertCard({ alert, onEdit }: Props) {
  const [confirmDelete, setConfirmDelete] = useState(false)
  const deleteMut = useDeleteAlert()

  const directionLabel = alert.direction === 'ABOVE' ? 'rises above' : 'falls below'
  const directionTone = alert.direction === 'ABOVE' ? 'success' : 'warning'

  const handleDelete = () => {
    if (!confirmDelete) {
      setConfirmDelete(true)
      // Auto-revert the confirm state after a few seconds if they don't click.
      setTimeout(() => setConfirmDelete(false), 3500)
      return
    }
    deleteMut.mutate(alert.id)
  }

  return (
    <div className="bg-white border border-ink-200 rounded-lg p-5">
      <div className="flex items-start justify-between gap-4">
        {/* Left: ticker + rule */}
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <span className="font-mono-num text-sm font-semibold text-ink-700 tracking-wider">
              {alert.ticker}
            </span>
            <Badge tone={directionTone}>{alert.direction}</Badge>
            {!alert.enabled && <Badge tone="muted">Disabled</Badge>}
          </div>

          <p className="text-sm text-ink-600 mb-3">
            Alert when <span className="font-medium">{alert.ticker}</span>{' '}
            {directionLabel}{' '}
            <span className="font-mono-num font-semibold text-ink-900">
              {formatPrice(alert.thresholdPrice)}
            </span>
          </p>

          <div className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-ink-400">
            <span>
              Cooldown:{' '}
              <span className="text-ink-600 font-mono-num">
                {formatCooldown(alert.cooldownSeconds)}
              </span>
            </span>
            <span>
              Channels:{' '}
              <span className="text-ink-600">
                {alert.channels.map((c) => (c === 'IN_APP' ? 'in-app' : 'email')).join(', ')}
              </span>
            </span>
            <span>
              Updated{' '}
              <span className="text-ink-600">{formatRelative(alert.updatedAt)}</span>
            </span>
          </div>
        </div>

        {/* Right: actions */}
        <div className="flex gap-2 shrink-0">
          <Button size="sm" variant="ghost" onClick={onEdit}>
            Edit
          </Button>
          <Button
            size="sm"
            variant="danger"
            onClick={handleDelete}
            disabled={deleteMut.isPending}
          >
            {deleteMut.isPending
              ? 'Deleting…'
              : confirmDelete
                ? 'Confirm?'
                : 'Delete'}
          </Button>
        </div>
      </div>
    </div>
  )
}