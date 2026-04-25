import { useQuery } from '@tanstack/react-query'
import { tickersApi } from '../../api'
import { PriceCard } from './PriceCard'
import { useAlerts } from '../alerts/hooks/useAlerts'

/**
 * Dashboard: current prices + at-a-glance alert counts.
 *
 * Refetches quotes every 30s — the backend poller runs every 60s, so 30s
 * on the frontend means at most ~30s staleness in the UI.
 */
export function DashboardPage() {
  const {
    data: quotes,
    isLoading: quotesLoading,
    error: quotesError,
  } = useQuery({
    queryKey: ['quotes'],
    queryFn: tickersApi.allQuotes,
    refetchInterval: 30_000,
  })

  const { data: alerts } = useAlerts()

  const activeCount = alerts?.filter((a) => a.enabled).length ?? 0
  const totalCount = alerts?.length ?? 0

  return (
    <div className="space-y-10">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-semibold tracking-tight mb-1">Dashboard</h1>
        <p className="text-sm text-ink-500">
          Current prices update every 60s.
        </p>
      </div>

      {/* Summary strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Stat label="Active alerts" value={activeCount.toString()} />
        <Stat label="Total configured" value={totalCount.toString()} />
        <Stat
          label="Tickers watched"
          value={(quotes?.length ?? 0).toString()}
        />
        <Stat
          label="Status"
          value={quotesError ? 'Disconnected' : 'Live'}
          tone={quotesError ? 'danger' : 'success'}
        />
      </div>

      {/* Ticker grid */}
      <div>
        <h2 className="text-xs font-medium uppercase tracking-wider text-ink-400 mb-4">
          Current Prices
        </h2>
        {quotesLoading && <p className="text-ink-400">Loading quotes…</p>}
        {quotesError && (
          <p className="text-danger text-sm">
            Couldn't load quotes: {(quotesError as Error).message}
          </p>
        )}
        {quotes && quotes.length === 0 && (
          <p className="text-ink-400 text-sm">
            No cached quotes yet. Wait for the first poll to complete (~60s after backend start).
          </p>
        )}
        {quotes && quotes.length > 0 && (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {quotes.map((q) => (
              <PriceCard key={q.ticker} quote={q} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function Stat({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone?: 'success' | 'danger'
}) {
  const toneClass =
    tone === 'success'
      ? 'text-success'
      : tone === 'danger'
        ? 'text-danger'
        : 'text-ink-800'

  return (
    <div className="bg-white border border-ink-200 rounded-lg px-4 py-3">
      <div className="text-[10px] uppercase tracking-wider text-ink-400 mb-1">
        {label}
      </div>
      <div className={`font-mono-num text-xl font-semibold tracking-tight ${toneClass}`}>
        {value}
      </div>
    </div>
  )
}