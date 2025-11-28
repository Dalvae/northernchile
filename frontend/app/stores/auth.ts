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
    loading: true
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
      const { showErrorToast, showSuccessToast, isAuthError } = useApiError()
      const { t } = useI18n()

      try {
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase

        // Call backend - token will be set in HttpOnly cookie automatically
        const response = await $fetch<{ user: User }>(`${apiBase}/api/auth/login`, {
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

          showSuccessToast(
            t('auth.welcome', '¡Bienvenido!'),
            t('auth.login_success', 'Has iniciado sesión correctamente.')
          )
        }
      } catch (error) {
        if (isAuthError(error)) {
          showErrorToast(error, t('auth.invalid_credentials', 'Credenciales inválidas'))
        } else {
          showErrorToast(error, t('auth.login_error', 'Error de autenticación'))
        }
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
      const { showErrorToast, showSuccessToast, isStatusCode } = useApiError()
      const { t } = useI18n()

      try {
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase
        await $fetch(`${apiBase}/api/auth/register`, {
          method: 'POST',
          body: userData,
          headers: {
            'Content-Type': 'application/json'
          }
        })

        showSuccessToast(
          t('auth.register_success', 'Registro Exitoso'),
          t('auth.register_success_description', 'Tu cuenta ha sido creada. Ahora puedes iniciar sesión.')
        )
      } catch (error) {
        if (isStatusCode(error, 409)) {
          showErrorToast(error, t('auth.email_in_use', 'El correo electrónico ya está en uso'))
        } else {
          showErrorToast(error, t('auth.register_error', 'Error de registro'))
        }
        throw error
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase

        // Call backend logout to clear cookie
        await $fetch(`${apiBase}/api/auth/logout`, {
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
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase

        const response = await $fetch<User>(`${apiBase}/api/profile/me`, {
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
      } catch (error) {
        console.error('[Auth] Error fetching user profile:', error)
        // If fetch fails (e.g., 401), clear user state
        this.user = null
      }
    },

    // Initialize auth by fetching user from backend (cookie-based)
    async initializeAuth() {
      this.loading = true
      try {
        // Try to fetch user profile - if cookie exists, backend will authenticate
        await this.fetchUser()
      } catch (error) {
        console.error('[Auth] Error in initializeAuth:', error)
        this.user = null
      } finally {
        this.loading = false
      }
    }
  }
})
