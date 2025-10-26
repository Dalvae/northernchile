export default defineNuxtRouteMiddleware(async (to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (to.path.startsWith("/admin")) {
    if (process.client) {
      const authStore = useAuthStore();

      // Ensure the auth state is loaded from localStorage
      // This is crucial for client-side navigation after initial load
      if (!authStore.isAuthenticated && localStorage.getItem("auth_token")) {
        authStore.checkAuth();
      }

      // Verificar autenticación
      if (!authStore.isAuthenticated) {
        return navigateTo("/auth");
      }

      // Verificar rol de admin
      const userRoles = authStore.user?.role || [];
      const hasAdminAccess = userRoles.includes("ROLE_SUPER_ADMIN") || userRoles.includes("ROLE_PARTNER_ADMIN");

      if (to.path === '/admin/users') {
        if (!userRoles.includes("ROLE_SUPER_ADMIN")) {
          return navigateTo("/");
        }
      }

      if (!hasAdminAccess) {
        return navigateTo("/");
      }
    }
  }
});
