import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { historyApi } from '../../../api'

/**
 * Paginated history query.
 *
 * keepPreviousData: while the next page loads, the previous page stays visible
 * — avoids a jarring "loading…" flash on every page change.
 */
export function useHistory(page: number, size = 20) {
  return useQuery({
    queryKey: ['history', page, size],
    queryFn: () => historyApi.list(page, size),
    placeholderData: keepPreviousData,
  })
}