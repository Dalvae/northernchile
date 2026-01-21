export default defineNuxtRouteMiddleware(async (to, _from) => {
  const authStore = useAuthStore()

  // Wait for auth to initialize (client-side only)
  if (import.meta.client && authStore.loading) {
    await new Promise<void>((resolve) => {
      const unwatch = watch(
        () => authStore.loading,
        (isLoading) => {
          if (!isLoading) {
            unwatch()
            resolve()
          }
        },
        { immediate: true }
      )

      // Safety timeout after 5 seconds
      setTimeout(() => {
        unwatch()
        resolve()
      }, 5000)
    })
  }

  // Check if user is authenticated
  if (!authStore.isAuthenticated) {
    // Redirect to login with return URL
    return navigateTo({
      path: '/auth',
      query: { redirect: to.fullPath }
    })
  }

  // Check if user has admin role
  const userRole = authStore.user?.role
  const isAdmin = userRole === 'ROLE_SUPER_ADMIN' || userRole === 'ROLE_PARTNER_ADMIN'

  if (!isAdmin) {
    // Redirect to homepage with error message
    const toast = useToast()
    toast.add({
      title: 'Acceso Denegado',
      description: 'No tienes permisos para acceder a esta página',
      color: 'error'
    })
    return navigateTo('/')
  }
})
