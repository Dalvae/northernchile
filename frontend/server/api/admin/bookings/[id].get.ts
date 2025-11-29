import type { BookingRes } from 'api-client'

export default defineEventHandler(async (event): Promise<BookingRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const bookingId = getRouterParam(event, 'id')

  try {
    const booking = await $fetch<BookingRes>(`${backendUrl}/api/admin/bookings/${bookingId}`, {
      headers: { Cookie: cookie }
    })
    return booking
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch booking'
    })
  }
})
