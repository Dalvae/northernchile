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

interface JwtPayload {
  sub: string
  email: string
  fullName: string
  role: string
  iat?: number
  exp?: number
  [key: string]: unknown
}

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const payloadBase64 = token.split('.')[1]
    if (!payloadBase64) return null
    const decodedJson = atob(
      payloadBase64.replace(/-/g, '+').replace(/_/g, '/')
    )
    return JSON.parse(decodedJson) as JwtPayload
  } catch (error) {
    return null
  }
}

function getFromStorage<T = unknown>(key: string): T | null {
  if (typeof window === 'undefined') return null
  try {
    const item = localStorage.getItem(key)
    return item ? (JSON.parse(item) as T) : null
  } catch {
    return null
  }
}

function setToStorage<T = unknown>(key: string, value: T | null): void {
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
        state.user.role === 'ROLE_SUPER_ADMIN'
        || state.user.role === 'ROLE_PARTNER_ADMIN'
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
          // Guardar en el state y en localStorage
          this.token = response.token
          // Save token as plain string (not JSON stringified)
          if (import.meta.client) {
            localStorage.setItem('auth_token', response.token)
          }

          // Backend now returns user object in response - use it directly
          if (response.user) {
            this.user = {
              id: response.user.id,
              email: response.user.email,
              fullName: response.user.fullName,
              role: response.user.role, // Now singular string from backend
              nationality: response.user.nationality,
              phoneNumber: response.user.phoneNumber,
              dateOfBirth: response.user.dateOfBirth
            }
            setToStorage('user', this.user)
          } else {
            // Fallback: decode from JWT if user not in response (shouldn't happen with new backend)
            const payload = decodeJwtPayload(response.token)
            if (payload) {
              // JWT still has roles as array (Spring Security convention)
              // Extract first role or use default
              const roleFromJwt = Array.isArray(payload.roles) ? payload.roles[0] : payload.roles
              this.user = {
                id: payload.userId || payload.sub,
                email: payload.email,
                fullName: payload.fullName || '',
                role: roleFromJwt || 'ROLE_CLIENT'
              }
              setToStorage('user', this.user)
            }
          }

          toast.add({
            title: '¡Bienvenido!',
            description: 'Has iniciado sesión correctamente.',
            color: 'success'
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
          color: 'error'
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
          color: 'success'
        })
      } catch (error: any) {
        let errorMessage = error.data?.message || 'Error en el registro'
        if (error.statusCode === 409) {
          errorMessage = 'El correo electrónico ya está en uso.'
        }
        toast.add({
          title: 'Error de Registro',
          description: errorMessage,
          color: 'error'
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
      // Remove token as plain string
      if (import.meta.client) {
        localStorage.removeItem('auth_token')
      }
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
            role: response.role, // Backend returns singular string
            nationality: response.nationality,
            phoneNumber: response.phoneNumber,
            dateOfBirth: response.dateOfBirth,
            authProvider: response.authProvider
          }
          setToStorage('user', this.user)
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
        // Read token as plain string (not JSON parsed)
        const savedToken = import.meta.client ? localStorage.getItem('auth_token') : null
        const savedUser = getFromStorage('user')

        if (savedToken) {
          const payload = decodeJwtPayload(savedToken)
          if (payload && payload.exp) {
            const currentTime = Math.floor(Date.now() / 1000)
            if (payload.exp < currentTime) {
              // Token expirado
              this.token = null
              this.user = null
              if (import.meta.client) {
                localStorage.removeItem('auth_token')
              }
              setToStorage('user', null)
            } else {
              // Token válido
              this.token = savedToken

              // Sincronizar user desde localStorage o decodificar del token
              if (savedUser && savedUser.role) {
                this.user = savedUser
              } else {
                // Reconstruir desde el token si no hay user en localStorage
                // JWT still has roles as array (Spring Security convention)
                const roleFromJwt = Array.isArray(payload.roles) ? payload.roles[0] : payload.roles
                this.user = {
                  id: payload.userId || payload.sub,
                  email: payload.email,
                  fullName: payload.fullName || '',
                  role: roleFromJwt || 'ROLE_CLIENT'
                }
                setToStorage('user', this.user)
              }
            }
          } else {
            // Token inválido
            this.token = null
            this.user = null
            if (import.meta.client) {
              localStorage.removeItem('auth_token')
            }
            setToStorage('user', null)
          }
        } else {
        }
      } catch (error) {
        console.error('[Auth] Error in initializeAuth:', error)
        this.token = null
        this.user = null
        if (import.meta.client) {
          localStorage.removeItem('auth_token')
        }
        setToStorage('user', null)
      } finally {
        this.loading = false
      }
    }
  }
})
