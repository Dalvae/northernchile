import { useAuthStore } from '~/stores/auth'

export default defineNuxtPlugin((nuxtApp) => {
  if (import.meta.client) {
    const authStore = useAuthStore()
    authStore.initializeAuth()

    // Interceptor global para manejar errores 401
    nuxtApp.hook('app:error', (error: any) => {
      if (error?.statusCode === 401 || error?.response?.status === 401) {
        authStore.logout()
      }
    })

    // Interceptor para fetch
    const originalFetch = globalThis.$fetch
    globalThis.$fetch = async (...args: any[]) => {
      try {
        return await originalFetch(...args)
      } catch (error: any) {
        if (error?.statusCode === 401 || error?.response?.status === 401) {
          authStore.logout()
        }
        throw error
      }
    }
  }
})
