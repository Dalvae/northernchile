import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isAuthenticated: false,
    user: null as { id: number; email: string; role: string[] } | null,
    token: null as string | null
  }),
  actions: {
    login(user: { id: number; email: string; role: string[] }, token: string) {
      this.isAuthenticated = true
      this.user = user
      this.token = token
      localStorage.setItem('token', token)
      // Aquí podrías guardar más datos del usuario si es necesario
    },
    logout() {
      this.isAuthenticated = false
      this.user = null
      this.token = null
      localStorage.removeItem('token')
      // Redirigir al login o a la página principal
      const router = useRouter()
      router.push('/login')
    },
    checkAuth() {
      const token = localStorage.getItem('token')
      if (token) {
        // Aquí podrías validar el token con el backend si es necesario
        // Por ahora, asumimos que si hay token, está autenticado
        this.isAuthenticated = true
        // Podrías decodificar el token para obtener datos del usuario
        // this.user = decodeToken(token)
      }
    }
  },
  getters: {
    // Puedes añadir getters si necesitas datos derivados del estado
  }
})
