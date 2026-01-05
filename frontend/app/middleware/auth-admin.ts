export default defineNuxtRouteMiddleware((to, _from) => {
  const authStore = useAuthStore()

  // Check if user is authenticated
  if (!authStore.isAuthenticated) {
    // Redirect to login with return URL
    return navigateTo({
      path: '/auth/login',
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
      title: 'Access Denied',
      description: 'You do not have permission to access this page',
      color: 'error'
    })
    return navigateTo('/')
  }
})
