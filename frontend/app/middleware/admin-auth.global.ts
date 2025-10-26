export default defineNuxtRouteMiddleware((to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (to.path.startsWith("/admin")) {
    if (process.client) {
      const authStore = useAuthStore();

      // Verificar autenticación
      if (!authStore.isAuthenticated) {
        return navigateTo("/auth");
      }

      // Verificar rol de admin
      const userRoles = authStore.user?.role || [];
      const hasAdminAccess = userRoles.includes("ROLE_SUPER_ADMIN") || userRoles.includes("ROLE_PARTNER_ADMIN");

      if (!hasAdminAccess) {
        return navigateTo("/");
      }
    }
  }
});
