export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const tourId = getRouterParam(event, 'id');
  const body = await readBody(event);

  try {
    const updatedTour = await $fetch(`${backendUrl}/api/tours/${tourId}`, {
      method: 'PUT',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body: body,
    });
    return updatedTour;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar tour',
    });
  }
});