import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware(async (to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (to.path.startsWith('/admin')) {
    const authStore = useAuthStore()

    // Si el store de autenticación está cargando, no hacer nada y esperar.
    if (authStore.loading) {
      return
    }

    // Verificar autenticación
    if (!authStore.isAuthenticated) {
      return navigateTo('/auth')
    }

    // Verificar rol de admin usando el getter isAdmin
    if (!authStore.isAdmin) {
      return navigateTo('/')
    }

    // Specific check for /admin/users, if needed, can be re-added here
    // if (to.path === '/admin/users') {
    //   if (!authStore.user?.role.includes("ROLE_SUPER_ADMIN")) {
    //     return navigateTo("/");
    //   }
    // }
  }
})
