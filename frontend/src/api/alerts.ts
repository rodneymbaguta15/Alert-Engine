import { apiClient } from './client'
import type {
  AlertConfigResponse,
  CreateAlertRequest,
  UpdateAlertRequest,
} from '../types/api'

/**
 * Thin wrappers around the alerts REST endpoints.
 * One function per endpoint, returning unwrapped data (axios response.data).
 * React Query hooks in features/alerts/ will call these.
 */

export const alertsApi = {
  list: async (): Promise<AlertConfigResponse[]> => {
    const { data } = await apiClient.get<AlertConfigResponse[]>('/alerts')
    return data
  },

  get: async (id: number): Promise<AlertConfigResponse> => {
    const { data } = await apiClient.get<AlertConfigResponse>(`/alerts/${id}`)
    return data
  },

  create: async (payload: CreateAlertRequest): Promise<AlertConfigResponse> => {
    const { data } = await apiClient.post<AlertConfigResponse>('/alerts', payload)
    return data
  },

  update: async (id: number, payload: UpdateAlertRequest): Promise<AlertConfigResponse> => {
    const { data } = await apiClient.put<AlertConfigResponse>(`/alerts/${id}`, payload)
    return data
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/alerts/${id}`)
  },
}