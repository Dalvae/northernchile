import { defineStore } from 'pinia'

interface User {
  id: string
  email: string
  fullName: string
  role: string[]
  nationality?: string
  phoneNumber?: string
  dateOfBirth?: string
  authProvider?: string
}

// Función auxiliar para decodificar el payload del JWT de forma segura
function decodeJwtPayload(token: string): any | null {
  try {
    const payloadBase64 = token.split('.')[1]
    if (!payloadBase64) return null
    const decodedJson = atob(
      payloadBase64.replace(/-/g, '+').replace(/_/g, '/')
    )
    return JSON.parse(decodedJson)
  } catch (error) {
    return null
  }
}

// Helper functions for localStorage (solo en cliente)
function getFromStorage(key: string): any {
  if (typeof window === 'undefined') return null
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : null
  } catch {
    return null
  }
}

function setToStorage(key: string, value: any): void {
  if (typeof window === 'undefined') return
  try {
    if (value === null) {
      localStorage.removeItem(key)
    } else {
      localStorage.setItem(key, JSON.stringify(value))
    }
  } catch (error) {
    console.error('Error saving to localStorage:', error)
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null as string | null,
    user: null as User | null,
    loading: true
  }),

  getters: {
    // `isAuthenticated` ahora es un getter que reacciona a los cambios en `token`.
    isAuthenticated(state): boolean {
      return !!state.token && !!state.user
    },
    isAdmin(state): boolean {
      if (!state.user?.role) return false
      return (
        state.user.role.includes('ROLE_SUPER_ADMIN')
        || state.user.role.includes('ROLE_PARTNER_ADMIN')
      )
    }
  },

  actions: {
    async login(credentials: { email: string, password: string }) {
      this.loading = true
      const toast = useToast()
      try {
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase

        // Llamada directa al backend Java
        const response = await $fetch<any>(`${apiBase}/api/auth/login`, {
          method: 'POST',
          body: credentials
        })

        if (response && response.token) {
          const payload = decodeJwtPayload(response.token)

          // Guardar en el state y en localStorage
          this.token = response.token
          setToStorage('auth_token', response.token)

          if (payload) {
            this.user = {
              id: payload.userId || payload.sub,
              email: payload.email,
              fullName: payload.fullName || '',
              role: payload.roles || []
            }
            setToStorage('user', this.user)
          }

          toast.add({
            title: '¡Bienvenido!',
            description: 'Has iniciado sesión correctamente.',
            color: 'green'
          })
        }
      } catch (error: any) {
        let errorMessage = error.data?.message || 'Error en el login'
        if (error.statusCode === 403 || error.statusCode === 401) {
          errorMessage = 'Credenciales inválidas.'
        }
        toast.add({
          title: 'Error de Autenticación',
          description: errorMessage,
          color: 'red'
        })
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
      const toast = useToast()
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

        toast.add({
          title: 'Registro Exitoso',
          description: 'Tu cuenta ha sido creada. Ahora puedes iniciar sesión.',
          color: 'green'
        })
      } catch (error: any) {
        let errorMessage = error.data?.message || 'Error en el registro'
        if (error.statusCode === 409) {
          errorMessage = 'El correo electrónico ya está en uso.'
        }
        toast.add({
          title: 'Error de Registro',
          description: errorMessage,
          color: 'red'
        })
        throw error
      } finally {
        this.loading = false
      }
    },

    async logout() {
      // Limpiar state y localStorage
      this.token = null
      this.user = null
      setToStorage('auth_token', null)
      setToStorage('user', null)

      // Solo redirigir si estamos en el cliente y no estamos ya en /auth
      if (import.meta.client) {
        const currentPath = window.location.pathname
        if (!currentPath.startsWith('/auth')) {
          await navigateTo('/auth')
        }
      }
    },

    // Cargar datos completos del usuario desde el backend
    async fetchUser() {
      if (!this.token) {
        console.log('[Auth] No token available, cannot fetch user')
        return
      }

      try {
        const config = useRuntimeConfig()
        const apiBase = config.public.apiBase

        const response = await $fetch<any>(`${apiBase}/api/profile/me`, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${this.token}`
          }
        })

        if (response) {
          this.user = {
            id: response.id,
            email: response.email,
            fullName: response.fullName,
            role: Array.isArray(response.role) ? response.role : [response.role],
            nationality: response.nationality,
            phoneNumber: response.phoneNumber,
            dateOfBirth: response.dateOfBirth,
            authProvider: response.authProvider
          }
          setToStorage('user', this.user)
          console.log('[Auth] User profile refreshed:', this.user.email)
        }
      } catch (error) {
        console.error('[Auth] Error fetching user profile:', error)
      }
    },

    // Inicializar desde localStorage al cargar la app
    initializeAuth() {
      this.loading = true
      try {
        // Cargar desde localStorage si está disponible
        const savedToken = getFromStorage('auth_token')
        const savedUser = getFromStorage('user')

        if (savedToken) {
          const payload = decodeJwtPayload(savedToken)
          if (payload && payload.exp) {
            const currentTime = Math.floor(Date.now() / 1000)
            if (payload.exp < currentTime) {
              // Token expirado
              console.log('[Auth] Token expired, clearing session')
              this.token = null
              this.user = null
              setToStorage('auth_token', null)
              setToStorage('user', null)
            } else {
              // Token válido
              this.token = savedToken

              // Sincronizar user desde localStorage o decodificar del token
              if (savedUser && savedUser.role && savedUser.role.length > 0) {
                this.user = savedUser
                console.log('[Auth] User loaded from localStorage:', savedUser.email, 'Role:', savedUser.role)
              } else {
                // Reconstruir desde el token si no hay user en localStorage
                console.log('[Auth] Reconstructing user from token')
                this.user = {
                  id: payload.userId || payload.sub,
                  email: payload.email,
                  fullName: payload.fullName || '',
                  role: payload.roles || []
                }
                setToStorage('user', this.user)
              }

              console.log('[Auth] User authenticated:', this.user?.email, 'Role:', this.user?.role)
            }
          } else {
            // Token inválido
            console.log('[Auth] Invalid token, clearing session')
            this.token = null
            this.user = null
            setToStorage('auth_token', null)
            setToStorage('user', null)
          }
        } else {
          console.log('[Auth] No token found in localStorage')
        }
      } catch (error) {
        console.error('[Auth] Error in initializeAuth:', error)
        this.token = null
        this.user = null
        setToStorage('auth_token', null)
        setToStorage('user', null)
      } finally {
        this.loading = false
      }
    }
  }
})
