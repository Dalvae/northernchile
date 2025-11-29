export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const bookingId = getRouterParam(event, 'id')
  const body = await readBody(event)

  try {
    const updatedBooking = await $fetch(`${backendUrl}/api/admin/bookings/${bookingId}`, {
      method: 'PUT',
      headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
      body
    })
    return updatedBooking
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to update booking'
    })
  }
})
