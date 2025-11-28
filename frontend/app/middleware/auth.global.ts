export default defineNuxtRouteMiddleware(async (to, _from) => {
  // Rutas protegidas que requieren autenticación (sin /admin, que lo maneja admin-auth.global.ts)
  const protectedRoutes = ['/profile', '/bookings']
  const isProtectedRoute = protectedRoutes.some(route => to.path.startsWith(route))

  // Solo procesar rutas protegidas
  if (!isProtectedRoute) {
    return
  }

  const authStore = useAuthStore()

  // Esperar a que la autenticación se inicialice (solo en cliente)
  // El plugin auth.ts ya llama initializeAuth() una vez al cargar la app
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

  if (!authStore.isAuthenticated && import.meta.client) {
    return navigateTo('/auth')
  }
})
