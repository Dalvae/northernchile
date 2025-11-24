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

    // Interceptor para fetch - automatically include cookies
    const originalFetch = globalThis.$fetch

    const wrappedFetch = (async (url: any, options: any = {}) => {
      // Ensure credentials are included for cookie-based auth
      const enhancedOptions = {
        ...options,
        credentials: options.credentials || 'include'
      }

      try {
        return await (originalFetch as any)(url, enhancedOptions)
      } catch (error: any) {
        if (error?.statusCode === 401 || error?.response?.status === 401) {
          authStore.logout()
        }
        throw error
      }
    }) as typeof originalFetch

    wrappedFetch.raw = originalFetch.raw
    wrappedFetch.create = originalFetch.create

    globalThis.$fetch = wrappedFetch
  }
})
