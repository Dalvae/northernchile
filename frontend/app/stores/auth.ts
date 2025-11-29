import { defineStore } from 'pinia'

interface User {
  id: string
  email: string
  fullName: string
  role: string
  nationality?: string
  phoneNumber?: string
  dateOfBirth?: string
  authProvider?: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
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
    }
  },

  actions: {
    async login(credentials: { email: string, password: string }) {
      this.loading = true

      try {
        // Call through Nuxt server API to properly forward cookies
        const response = await $fetch<{ user: User }>('/api/auth/login', {
          method: 'POST',
          body: credentials,
          credentials: 'include'
        })

        // Backend now returns user object (token is in cookie, not in response)
        if (response?.user) {
          this.user = {
            id: response.user.id,
            email: response.user.email,
            fullName: response.user.fullName,
            role: response.user.role,
            nationality: response.user.nationality,
            phoneNumber: response.user.phoneNumber,
            dateOfBirth: response.user.dateOfBirth
          }
        }
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },

    async register(userData: {
      email: string
      password: string
      fullName: string
      phoneNumber?: string | null
      nationality?: string | null
    }) {
      this.loading = true

      try {
        // Call through Nuxt server API
        await $fetch('/api/auth/register', {
          method: 'POST',
          body: userData
        })
      } catch (error) {
        throw error
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        // Call through Nuxt server API to properly clear cookies
        await $fetch('/api/auth/logout', {
          method: 'POST',
          credentials: 'include'
        })
      } catch (error) {
        console.error('Logout error:', error)
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
        const response = await $fetch<User>('/api/profile/me', {
          method: 'GET',
          credentials: 'include'
        })

        if (response) {
          this.user = {
            id: response.id,
            email: response.email,
            fullName: response.fullName,
            role: response.role,
            nationality: response.nationality,
            phoneNumber: response.phoneNumber,
            dateOfBirth: response.dateOfBirth,
            authProvider: response.authProvider
          }
        }
      } catch (error: unknown) {
        // 401/403 are expected when not authenticated - don't log as error
        const status = (error as { statusCode?: number })?.statusCode
          || (error as { response?: { status?: number } })?.response?.status
        if (status !== 401 && status !== 403) {
          console.error('[Auth] Error fetching user profile:', error)
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
