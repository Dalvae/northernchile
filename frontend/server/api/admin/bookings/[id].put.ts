export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const bookingId = getRouterParam(event, 'id');
  const body = await readBody(event);

  try {
    const updatedBooking = await $fetch(`${backendUrl}/api/bookings/${bookingId}`, {
      method: 'PUT',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body: body,
    });
    return updatedBooking;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar reserva',
    });
  }
});