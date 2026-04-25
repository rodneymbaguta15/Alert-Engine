import type { PriceQuoteResponse } from '../../types/api'
import { formatPrice, formatRelative, cn } from '../../lib/format'

/**
 * Single ticker card on the dashboard grid.
 *
 * Shows current price, day's change (absolute + percent) derived from
 * previousClose, and the cache freshness. No chart — this is a price monitor,
 * not a charting tool. Traders can open their real broker for charts.
 */
export function PriceCard({ quote }: { quote: PriceQuoteResponse }) {
  const current = Number(quote.currentPrice)
  const prevClose = quote.previousClose ? Number(quote.previousClose) : null

  // Compute change only if we have a previous close — otherwise show em-dash.
  let changeAbs: number | null = null
  let changePct: number | null = null
  if (prevClose !== null && Number.isFinite(prevClose) && prevClose !== 0) {
    changeAbs = current - prevClose
    changePct = (changeAbs / prevClose) * 100
  }

  const isUp = changeAbs !== null && changeAbs > 0
  const isDown = changeAbs !== null && changeAbs < 0

  return (
    <div className="bg-white border border-ink-200 rounded-lg p-5 hover:border-ink-300 transition-colors">
      {/* Header: ticker + freshness */}
      <div className="flex items-baseline justify-between mb-3">
        <span className="font-mono-num text-sm font-semibold text-ink-600 tracking-wider">
          {quote.ticker}
        </span>
        <span
          className="text-[10px] uppercase tracking-wider text-ink-400"
          title={quote.fetchedAt}
        >
          {formatRelative(quote.fetchedAt)}
        </span>
      </div>

      {/* Current price — the hero number */}
      <div className="font-mono-num text-3xl font-semibold tracking-tight mb-2">
        {formatPrice(quote.currentPrice)}
      </div>

      {/* Change */}
      <div className="flex items-center gap-3 text-sm">
        {changeAbs !== null && changePct !== null ? (
          <>
            <span
              className={cn(
                'font-mono-num font-medium',
                isUp && 'text-success',
                isDown && 'text-danger',
                !isUp && !isDown && 'text-ink-500',
              )}
            >
              {changeAbs >= 0 ? '+' : ''}
              {changeAbs.toFixed(2)}
            </span>
            <span
              className={cn(
                'font-mono-num text-xs',
                isUp && 'text-success',
                isDown && 'text-danger',
                !isUp && !isDown && 'text-ink-400',
              )}
            >
              ({changePct >= 0 ? '+' : ''}
              {changePct.toFixed(2)}%)
            </span>
          </>
        ) : (
          <span className="text-ink-400 text-xs">No previous close</span>
        )}
      </div>

      {/* Secondary stats: H / L / O */}
      <div className="grid grid-cols-3 gap-4 mt-4 pt-4 border-t border-ink-100">
        <Stat label="Open" value={quote.openPrice} />
        <Stat label="High" value={quote.highPrice} />
        <Stat label="Low" value={quote.lowPrice} />
      </div>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string | null }) {
  return (
    <div>
      <div className="text-[10px] uppercase tracking-wider text-ink-400 mb-0.5">
        {label}
      </div>
      <div className="font-mono-num text-sm text-ink-700">
        {value ? formatPrice(value) : '—'}
      </div>
    </div>
  )
}