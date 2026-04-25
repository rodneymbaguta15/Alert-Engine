import type { AlertHistoryResponse, DeliveryStatus } from '../../types/api'
import { formatPrice, formatTimestamp } from '../../lib/format'
import { Badge } from '../../components/Badge'

/**
 * History as a dense table — better than cards for scanning many rows.
 *
 * Single DOM table; pagination handled by the parent page.
 */
interface Props {
  rows: AlertHistoryResponse[]
}

export function HistoryTable({ rows }: Props) {
  if (rows.length === 0) {
    return (
      <div className="bg-white border border-ink-200 rounded-lg p-10 text-center">
        <p className="text-ink-500">No history yet.</p>
        <p className="text-ink-400 text-sm mt-1">
          Alert attempts (sent, failed, or suppressed) will appear here.
        </p>
      </div>
    )
  }

  return (
    <div className="bg-white border border-ink-200 rounded-lg overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-ink-50 border-b border-ink-200">
          <tr>
            <Th>Time</Th>
            <Th>Ticker</Th>
            <Th align="right">Price</Th>
            <Th align="right">Threshold</Th>
            <Th>Direction</Th>
            <Th>Channel</Th>
            <Th>Status</Th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.id} className="border-b border-ink-100 last:border-0 hover:bg-ink-50/50">
              <Td>
                <span className="text-ink-600" title={r.triggeredAt}>
                  {formatTimestamp(r.triggeredAt)}
                </span>
              </Td>
              <Td>
                <span className="font-mono-num font-semibold text-ink-700 text-xs tracking-wider">
                  {r.ticker}
                </span>
              </Td>
              <Td align="right">
                <span className="font-mono-num text-ink-800">
                  {formatPrice(r.triggeredPrice)}
                </span>
              </Td>
              <Td align="right">
                <span className="font-mono-num text-ink-500">
                  {formatPrice(r.thresholdPrice)}
                </span>
              </Td>
              <Td>
                <span className="text-xs uppercase tracking-wider text-ink-500">
                  {r.direction.toLowerCase()}
                </span>
              </Td>
              <Td>
                <span className="text-xs text-ink-600">
                  {r.channel === 'IN_APP' ? 'in-app' : 'email'}
                </span>
              </Td>
              <Td>
                <StatusBadge status={r.deliveryStatus} error={r.errorMessage} />
              </Td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function StatusBadge({
  status,
  error,
}: {
  status: DeliveryStatus
  error: string | null
}) {
  const config: Record<DeliveryStatus, { tone: 'success' | 'danger' | 'muted'; label: string }> = {
    SENT: { tone: 'success', label: 'Sent' },
    FAILED: { tone: 'danger', label: 'Failed' },
    SUPPRESSED_COOLDOWN: { tone: 'muted', label: 'Cooldown' },
  }
  const { tone, label } = config[status]
  return (
    <Badge tone={tone} className={error ? 'cursor-help' : ''}>
      <span title={error ?? undefined}>{label}</span>
    </Badge>
  )
}

function Th({ children, align }: { children: React.ReactNode; align?: 'right' }) {
  return (
    <th
      className={
        'px-4 py-2.5 text-[10px] font-medium uppercase tracking-wider text-ink-500 ' +
        (align === 'right' ? 'text-right' : 'text-left')
      }
    >
      {children}
    </th>
  )
}

function Td({ children, align }: { children: React.ReactNode; align?: 'right' }) {
  return (
    <td className={'px-4 py-3 ' + (align === 'right' ? 'text-right' : 'text-left')}>
      {children}
    </td>
  )
}