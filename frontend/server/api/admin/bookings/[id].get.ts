import type { BookingRes } from 'api-client'

export default defineEventHandler(async (event): Promise<BookingRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const bookingId = getRouterParam(event, 'id')

  try {
    const booking = await $fetch<BookingRes>(`${backendUrl}/api/bookings/${bookingId}`, {
      headers: { Authorization: authToken || '' }
    })
    return booking
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener reserva'
    })
  }
})
