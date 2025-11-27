export default defineNuxtRouteMiddleware((to, _from) => {
  const authStore = useAuthStore()

  // Solo ejecutar en el cliente
  if (import.meta.client) {
    authStore.initializeAuth()
  }

  // Rutas protegidas que requieren autenticación
  const protectedRoutes = ['/admin', '/profile', '/bookings']
  const isProtectedRoute = protectedRoutes.some(route => to.path.startsWith(route))

  if (isProtectedRoute && !authStore.isAuthenticated && import.meta.client) {
    return navigateTo('/auth')
  }
})
