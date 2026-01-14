export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, instance, info) => {
    // Only log detailed errors in development
    if (import.meta.dev) {
      console.group('🔴 Vue Error')
      console.error('Error:', error)
      console.error('Message:', error instanceof Error ? error.message : String(error))
      console.error('Stack:', error instanceof Error ? error.stack : 'No stack trace')
      console.error('Component:', instance?.$options?.name || 'Unknown')
      console.error('Info:', info)
      console.groupEnd()
    }

    if (import.meta.client) {
      const toast = useToast()
      const errorMessage = error instanceof Error ? error.message : String(error)

      toast.add({
        title: 'Error inesperado',
        description: import.meta.dev ? `${info}: ${errorMessage}` : 'Ha ocurrido un error',
        color: 'error'
      })
    }
  }

  nuxtApp.vueApp.config.warnHandler = (msg, instance, trace) => {
    if (import.meta.dev) {
      console.group('⚠️ Vue Warning')
      console.warn('Message:', msg)
      console.warn('Component:', instance?.$options?.name || 'Unknown')
      console.warn('Trace:', trace)
      console.groupEnd()
    }
  }

  nuxtApp.hook('vue:error', (error, instance, info) => {
    // Only log in development
    if (import.meta.dev) {
      console.group('🔴 Nuxt Vue Error Hook')
      console.error('Error:', error)
      console.error('Message:', error instanceof Error ? error.message : String(error))
      console.error('Stack:', error instanceof Error ? error.stack : 'No stack trace')
      console.error('Instance:', instance)
      console.error('Info:', info)
      console.groupEnd()
    }
  })
})
