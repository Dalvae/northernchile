import { defineStore } from 'pinia'
import type { UserRes } from 'api-client'
import logger from '~/utils/logger'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as UserRes | null,
    loading: true,
    initialized: false
  }),

  getters: {
    isAuthenticated(state): boolean {
      return !!state.user
    },
    isAdmin(state): boolean {
      if (!state.user?.role) return false
      return (
        state.user.role === 'ROLE_SUPER_ADMIN'
        || state.user.role === 'ROLE_PARTNER_ADMIN'
      )
    },
    isSuperAdmin(state): boolean {
      return state.user?.role === 'ROLE_SUPER_ADMIN'
    }
  },

  actions: {
    async login(credentials: { email: string, password: string }) {
      this.loading = true

      // Call through Nuxt server API to properly forward cookies
      const response = await $fetch<{ user: UserRes }>('/api/auth/login', {
        method: 'POST',
        body: credentials,
        credentials: 'include'
      })

      // Backend now returns user object (token is in cookie, not in response)
      if (response?.user) {
        this.user = response.user
      }
      this.loading = false
    },

    async register(userData: {
      email: string
      password: string
      fullName: string
      phoneNumber?: string | null
      nationality?: string | null
    }) {
      this.loading = true

      // Call through Nuxt server API
      await $fetch('/api/auth/register', {
        method: 'POST',
        body: userData
      })
      this.loading = false
    },

    async logout() {
      try {
        // Call through Nuxt server API to properly clear cookies
        await $fetch('/api/auth/logout', {
          method: 'POST',
          credentials: 'include'
        })
      } catch (error) {
        logger.error('Logout error:', error)
      }

      // Clear user state
      this.user = null

      // Redirect to auth page if not already there
      if (import.meta.client) {
        const currentPath = window.location.pathname
        if (!currentPath.startsWith('/auth')) {
          await navigateTo('/auth')
        }
      }
    },

    // Cargar datos completos del usuario desde el backend
    async fetchUser() {
      try {
        // Call through Nuxt server API
        const response = await $fetch<UserRes>('/api/profile/me', {
          method: 'GET',
          credentials: 'include'
        })

        if (response) {
          this.user = response
        }
      } catch (error: unknown) {
        // 401/403 are expected when not authenticated - don't log as error
        const status = (error as { statusCode?: number })?.statusCode
          || (error as { response?: { status?: number } })?.response?.status
        if (status !== 401 && status !== 403) {
          logger.error('[Auth] Error fetching user profile:', error)
        }
        this.user = null
      }
    },

    // Initialize auth by fetching user from backend (cookie-based)
    async initializeAuth() {
      // Prevent multiple initializations
      if (this.initialized) {
        return
      }

      this.loading = true
      try {
        // Try to fetch user profile - if cookie exists, backend will authenticate
        await this.fetchUser()
      } catch {
        // Errors are already handled in fetchUser
        this.user = null
      } finally {
        this.loading = false
        this.initialized = true
      }
    }
  }
})
