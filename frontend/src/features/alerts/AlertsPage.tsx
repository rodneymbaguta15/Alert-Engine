import { useState } from 'react'
import { useAlerts } from './hooks/useAlerts'
import { AlertList } from './AlertList'
import { AlertFormDialog } from './AlertFormDialog'
import { Button } from '../../components/Button'
import type { AlertConfigResponse } from '../../types/api'

export function AlertsPage() {
  const { data: alerts, isLoading, error } = useAlerts()

  // Dialog state: closed | create | edit(alert). Using a discriminated union
  // keeps the "which alert are we editing" info tied to the state atomically,
  // rather than needing two separate useStates (open + editingAlert).
  const [dialogState, setDialogState] = useState<
    { mode: 'closed' } | { mode: 'create' } | { mode: 'edit'; alert: AlertConfigResponse }
  >({ mode: 'closed' })

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight mb-1">Alerts</h1>
          <p className="text-sm text-ink-500">
            Configure price thresholds and notification channels.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setDialogState({ mode: 'create' })}
        >
          + New alert
        </Button>
      </div>

      {isLoading && <p className="text-ink-400 text-sm">Loading alerts…</p>}
      {error && (
        <p className="text-danger text-sm">
          Couldn't load alerts: {(error as Error).message}
        </p>
      )}
      {alerts && (
        <AlertList
          alerts={alerts}
          onEdit={(a) => setDialogState({ mode: 'edit', alert: a })}
        />
      )}

      <AlertFormDialog
        open={dialogState.mode !== 'closed'}
        onClose={() => setDialogState({ mode: 'closed' })}
        initial={dialogState.mode === 'edit' ? dialogState.alert : undefined}
      />
    </div>
  )
}