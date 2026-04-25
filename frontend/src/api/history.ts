import { apiClient } from './client'
import type { AlertHistoryResponse, Page } from '../types/api'

export const historyApi = {
  list: async (page = 0, size = 20): Promise<Page<AlertHistoryResponse>> => {
    const { data } = await apiClient.get<Page<AlertHistoryResponse>>('/history', {
      params: { page, size },
    })
    return data
  },
}