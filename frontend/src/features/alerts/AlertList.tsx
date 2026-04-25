import type { AlertConfigResponse } from '../../types/api'
import { AlertCard } from './AlertCard'

interface Props {
  alerts: AlertConfigResponse[]
  onEdit: (alert: AlertConfigResponse) => void
}

export function AlertList({ alerts, onEdit }: Props) {
  if (alerts.length === 0) {
    return (
      <div className="bg-white border border-ink-200 rounded-lg p-10 text-center">
        <p className="text-ink-500 mb-1">No alerts configured yet.</p>
        <p className="text-ink-400 text-sm">
          Create your first price alert to start monitoring.
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {alerts.map((a) => (
        <AlertCard key={a.id} alert={a} onEdit={() => onEdit(a)} />
      ))}
    </div>
  )
}