export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;

  // Intentamos obtener el token de la cookie primero.
  const tokenFromCookie = getCookie(event, 'auth_token');
  
  if (!tokenFromCookie) {
    // Si no hay cookie, no hay sesión válida en el servidor.
    throw createError({
      statusCode: 401,
      message: 'No autenticado',
    });
  }

  const authToken = `Bearer ${tokenFromCookie}`;

  try {
    const tours = await $fetch(`${backendUrl}/api/admin/tours`, {
      headers: { 'Authorization': authToken }
    });
    return tours;
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al obtener tours de administración',
    });
  }
});