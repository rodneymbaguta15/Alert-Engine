import { apiClient } from './client'
import type { AuthUser } from '../store/authStore'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  user: AuthUser
}

export const authApi = {
  login: async (payload: LoginRequest): Promise<LoginResponse> => {
    const { data } = await apiClient.post<LoginResponse>('/auth/login', payload)
    return data
  },

  /** Used to verify token validity on app startup — see useEffect in App.tsx. */
  me: async (): Promise<AuthUser> => {
    const { data } = await apiClient.get<AuthUser>('/auth/me')
    return data
  },
}