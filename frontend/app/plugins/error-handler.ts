export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, instance, info) => {
    console.error('Vue Error:', error)
    console.error('Component:', instance?.$options?.name || 'Unknown')
    console.error('Info:', info)

    if (import.meta.client) {
      const toast = useToast()
      toast.add({
        title: 'Error inesperado',
        description: 'Ha ocurrido un error. Por favor, recarga la página.',
        color: 'error'
      })
    }
  }

  nuxtApp.vueApp.config.warnHandler = (msg, instance, trace) => {
    if (import.meta.dev) {
      console.warn('Vue Warning:', msg)
      console.warn('Component:', instance?.$options?.name || 'Unknown')
      console.warn('Trace:', trace)
    }
  }

  nuxtApp.hook('vue:error', (error, instance, info) => {
    console.error('Nuxt Vue Error Hook:', error)
    console.error('Instance:', instance)
    console.error('Info:', info)
  })
})
