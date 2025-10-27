export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const userId = getRouterParam(event, 'id');

  try {
    const user = await $fetch(`${backendUrl}/api/users/${userId}`, {
      headers: { 'Authorization': authToken || '' },
    });
    return user;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener usuario',
    });
  }
});