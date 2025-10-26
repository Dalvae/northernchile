export default defineNuxtRouteMiddleware((to) => {
  // Solo aplicar a rutas que empiecen con /admin
  if (to.path.startsWith("/admin")) {
    const authStore = useAuthStore();

    // Verificar autenticación
    if (!authStore.isAuthenticated) {
      return navigateTo("/auth");
    }

    // Verificar rol de admin
    if (!authStore.user?.role?.includes("ADMIN")) {
      return navigateTo("/");
    }
  }
});
