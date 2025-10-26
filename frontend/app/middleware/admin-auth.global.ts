export default defineNuxtRouteMiddleware((to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (to.path.startsWith("/admin")) {
    if (process.client) {
      const authStore = useAuthStore();

      console.log('Middleware (client) - Start:', { isAuthenticated: authStore.isAuthenticated, user: authStore.user });

      // Verificar autenticación
      if (!authStore.isAuthenticated) {
        console.log('Middleware (client) - Not authenticated, redirecting to /auth');
        return navigateTo("/auth");
      }

      // Verificar rol de admin
      const userRoles = authStore.user?.role || [];
      const hasAdminAccess = userRoles.includes("ROLE_SUPER_ADMIN") || userRoles.includes("ROLE_PARTNER_ADMIN");

      console.log('Middleware (client) - Authenticated, checking roles:', { userRoles, hasAdminAccess });

      if (!hasAdminAccess) {
        console.log('Middleware (client) - Not admin or partner admin, redirecting to /');
        return navigateTo("/");
      }
      console.log('Middleware (client) - Admin authenticated, proceeding.');
    }
  }
});
