export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const userId = getRouterParam(event, 'id');

  try {
    await $fetch(`${backendUrl}/api/users/${userId}`, {
      method: 'DELETE',
      headers: { 'Authorization': authToken || '' },
    });
    return { status: 'success', message: 'Usuario eliminado correctamente' };
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al eliminar usuario',
    });
  }
});