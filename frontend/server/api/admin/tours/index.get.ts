export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.backendApiUrl; // Use public runtime config
  const authToken = getHeader(event, 'Authorization'); // Pasa el token del admin

  try {
    // Llama al endpoint de admin del backend, no al público
    const tours = await $fetch(`${backendUrl}/api/tours`, { // Asegúrate de que el endpoint del backend sea el correcto
      headers: { 'Authorization': authToken || '' }
    });
    return tours;
  } catch (error: any) {
    // Manejo de errores...
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener tours de administración',
    });
  }
});