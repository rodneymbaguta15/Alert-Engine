import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/**
 * Auth state, persisted to localStorage so a page refresh keeps the user
 * logged in. Stores the JWT and a minimal user snapshot.
 *
 *
 */

export interface AuthUser {
  id: number
  email: string
  displayName: string | null
}

interface AuthState {
  token: string | null
  user: AuthUser | null
  isAuthenticated: () => boolean
  setAuth: (token: string, user: AuthUser) => void
  clear: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,

      // Function form so consumers always read the latest token without
      // needing a selector — useful for non-React code (axios interceptor).
      isAuthenticated: () => get().token !== null,

      setAuth: (token, user) => set({ token, user }),

      clear: () => set({ token: null, user: null }),
    }),
    {
      name: 'alert-engine-auth',  // localStorage key
      // Only persist the durable bits; functions don't serialize.
      partialize: (state) => ({ token: state.token, user: state.user }),
    },
  ),
)