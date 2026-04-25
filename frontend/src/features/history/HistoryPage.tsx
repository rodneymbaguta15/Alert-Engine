import { useState } from 'react'
import { useHistory } from './hooks/useHistory'
import { HistoryTable } from './HistoryTable'
import { Button } from '../../components/Button'

export function HistoryPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading, error, isFetching } = useHistory(page)

  const totalPages = data?.totalPages ?? 0
  const totalElements = data?.totalElements ?? 0

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight mb-1">History</h1>
        <p className="text-sm text-ink-500">
          Every alert delivery attempt, newest first.
        </p>
      </div>

      {isLoading && <p className="text-ink-400 text-sm">Loading…</p>}
      {error && (
        <p className="text-danger text-sm">
          Couldn't load history: {(error as Error).message}
        </p>
      )}

      {data && (
        <>
          <HistoryTable rows={data.content} />

          {totalPages > 1 && (
            <div className="flex items-center justify-between text-sm">
              <span className="text-ink-500">
                Page{' '}
                <span className="font-mono-num text-ink-800">{page + 1}</span>
                {' of '}
                <span className="font-mono-num text-ink-800">{totalPages}</span>
                <span className="text-ink-400 ml-2">({totalElements} total)</span>
              </span>

              <div className="flex gap-2 items-center">
                {isFetching && (
                  <span className="text-ink-400 text-xs mr-2">Updating…</span>
                )}
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  ← Prev
                </Button>
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages - 1}
                >
                  Next →
                </Button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}