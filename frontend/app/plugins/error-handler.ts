export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, instance, info) => {
    // Log completo del error
    console.group('🔴 Vue Error')
    console.error('Error:', error)
    console.error('Message:', error instanceof Error ? error.message : String(error))
    console.error('Stack:', error instanceof Error ? error.stack : 'No stack trace')
    console.error('Component:', instance?.$options?.name || 'Unknown')
    console.error('Info:', info)
    console.groupEnd()

    if (import.meta.client) {
      const toast = useToast()
      const errorMessage = error instanceof Error ? error.message : String(error)

      toast.add({
        title: 'Error inesperado',
        description: `${info}: ${errorMessage}`,
        color: 'error',
        timeout: 8000
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
    console.group('🔴 Nuxt Vue Error Hook')
    console.error('Error:', error)
    console.error('Message:', error instanceof Error ? error.message : String(error))
    console.error('Stack:', error instanceof Error ? error.stack : 'No stack trace')
    console.error('Instance:', instance)
    console.error('Info:', info)
    console.groupEnd()
  })
})
