import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { alertsApi } from '../../../api'
import type { CreateAlertRequest, UpdateAlertRequest } from '../../../types/api'

/**
 * React Query hooks for the alerts resource.
 *
 * Convention: one queryKey per resource, invalidated on every mutation.
 * A more granular approach (e.g., ['alerts', id] per item) would let us
 * update single items in the cache without refetching the list, but at this
 * scale (tens of alerts) the simple blanket invalidation is fine and easier
 * to reason about.
 */

const ALERTS_KEY = ['alerts'] as const

export function useAlerts() {
  return useQuery({
    queryKey: ALERTS_KEY,
    queryFn: alertsApi.list,
  })
}

export function useAlert(id: number | undefined) {
  return useQuery({
    queryKey: [...ALERTS_KEY, id],
    queryFn: () => alertsApi.get(id!),
    enabled: id !== undefined,
  })
}

export function useCreateAlert() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateAlertRequest) => alertsApi.create(payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ALERTS_KEY }),
  })
}

export function useUpdateAlert() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateAlertRequest }) =>
      alertsApi.update(id, payload),
    onSuccess: () => qc.invalidateQueries({ queryKey: ALERTS_KEY }),
  })
}

export function useDeleteAlert() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => alertsApi.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ALERTS_KEY }),
  })
}