import { create } from 'zustand'
import { User } from '../types'

interface UserState {
    user: User | null
    token: string | null
    setUser: (user: User) => void
    setToken: (token: string) => void
    logout: () => void
}

export const useUserStore = create<UserState>((set) => ({
    user: null,
    token: null,
    setUser: (user) => set({ user }),
    setToken: (token) => set({ token }),
    logout: () => set({ user: null, token: null }),
}))
