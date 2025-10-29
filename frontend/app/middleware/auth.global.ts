export default defineNuxtRouteMiddleware((to, from) => {
  const authStore = useAuthStore();
  
  // Solo ejecutar en el cliente
  if (process.client) {
    authStore.checkAuth();
  }
  
  // Rutas protegidas que requieren autenticación
  const protectedRoutes = ['/admin', '/profile', '/bookings'];
  const isProtectedRoute = protectedRoutes.some(route => to.path.startsWith(route));
  
  if (isProtectedRoute && !authStore.isAuthenticated && process.client) {
    return navigateTo('/auth');
  }
});