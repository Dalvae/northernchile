export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;
  const authToken = getHeader(event, 'Authorization');
  const bookingId = getRouterParam(event, 'id');

  try {
    await $fetch(`${backendUrl}/api/bookings/${bookingId}`, {
      method: 'DELETE',
      headers: { 'Authorization': authToken || '' },
    });
    return { status: 'success', message: 'Reserva eliminada correctamente' };
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al eliminar reserva',
    });
  }
});