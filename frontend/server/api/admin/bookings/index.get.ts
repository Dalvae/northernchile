export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;
  const authToken = getHeader(event, 'Authorization');
  const query = getQuery(event);

  try {
    const bookings = await $fetch(`${backendUrl}/api/bookings`, {
      headers: { 'Authorization': authToken || '' },
      query: query,
    });
    return bookings;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener reservas',
    });
  }
});