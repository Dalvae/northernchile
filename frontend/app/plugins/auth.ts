import { useAuthStore } from '~/stores/auth'

export default defineNuxtPlugin((nuxtApp) => {
  if (import.meta.client) {
    const authStore = useAuthStore()
    authStore.initializeAuth()

    // Interceptor global para manejar errores 401
    nuxtApp.hook('app:error', (error) => {
      const err = error as { statusCode?: number, response?: { status?: number } }
      if (err?.statusCode === 401 || err?.response?.status === 401) {
        authStore.logout()
      }
    })

    // Interceptor para fetch - automatically include cookies
    const originalFetch = globalThis.$fetch

    const wrappedFetch = (async (url: string, options: Parameters<typeof $fetch>[1] = {}) => {
      const enhancedOptions = {
        ...options,
        credentials: options.credentials || 'include' as const
      }

      try {
        return await originalFetch(url, enhancedOptions)
      } catch (error) {
        const err = error as { statusCode?: number, response?: { status?: number } }
        if (err?.statusCode === 401 || err?.response?.status === 401) {
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
