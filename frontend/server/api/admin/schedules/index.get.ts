export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const query = getQuery(event);

  try {
    const schedules = await $fetch(`${backendUrl}/api/tour-schedules`, {
      headers: { 'Authorization': authToken || '' },
      query: query,
    });
    return schedules;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener horarios de tours',
    });
  }
});