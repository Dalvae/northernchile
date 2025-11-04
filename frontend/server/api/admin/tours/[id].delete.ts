export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;
  const authToken = getHeader(event, 'Authorization');
  const tourId = getRouterParam(event, 'id');

  try {
    await $fetch(`${backendUrl}/api/admin/tours/${tourId}`, {
      method: 'DELETE',
      headers: { 'Authorization': authToken || '' },
    });
    return { status: 'success', message: 'Tour eliminado correctamente' };
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al eliminar tour',
    });
  }
});