export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const scheduleId = getRouterParam(event, 'id');
  const body = await readBody(event);

  try {
    const updatedSchedule = await $fetch(`${backendUrl}/api/tour-schedules/${scheduleId}`, {
      method: 'PUT',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body: body,
    });
    return updatedSchedule;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar horario de tour',
    });
  }
});