import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware(async (to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (!to.path.startsWith('/admin')) {
    return
  }

  const authStore = useAuthStore()

  // Esperar a que la autenticación se inicialice (solo en cliente)
  if (import.meta.client && authStore.loading) {
    await new Promise<void>((resolve) => {
      const checkLoading = () => {
        if (!authStore.loading) {
          resolve()
        } else {
          setTimeout(checkLoading, 50)
        }
      }
      checkLoading()
    })
  }

  // Verificar autenticación
  if (!authStore.isAuthenticated) {
    return navigateTo('/auth')
  }

  // Verificar rol de admin usando el getter isAdmin
  if (!authStore.isAdmin) {
    return navigateTo('/')
  }
})
