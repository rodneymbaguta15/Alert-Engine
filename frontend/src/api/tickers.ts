import { apiClient } from './client'
import type { PriceQuoteResponse } from '../types/api'

export const tickersApi = {
  listAllowed: async (): Promise<string[]> => {
    const { data } = await apiClient.get<string[]>('/tickers')
    return data
  },

  allQuotes: async (): Promise<PriceQuoteResponse[]> => {
    const { data } = await apiClient.get<PriceQuoteResponse[]>('/tickers/quotes')
    return data
  },

  quote: async (symbol: string): Promise<PriceQuoteResponse> => {
    const { data } = await apiClient.get<PriceQuoteResponse>(`/tickers/${symbol}/quote`)
    return data
  },
}