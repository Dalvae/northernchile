export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const scheduleId = getRouterParam(event, 'id');

  try {
    await $fetch(`${backendUrl}/api/tour-schedules/${scheduleId}`, {
      method: 'DELETE',
      headers: { 'Authorization': authToken || '' },
    });
    return { status: 'success', message: 'Horario de tour eliminado correctamente' };
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al eliminar horario de tour',
    });
  }
});