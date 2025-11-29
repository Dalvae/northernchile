export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const id = getRouterParam(event, 'id')

  const cookie = getHeader(event, 'cookie') || ''

  try {
    const response = await $fetch(`${backendUrl}/api/bookings/${id}`, {
      method: 'DELETE',
      headers: {
        'Cookie': cookie
      }
    })
    return response
  } catch (error: unknown) {
    const err = error as { statusCode?: number, statusMessage?: string, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.statusMessage || err.message || 'Failed to delete booking'
    })
  }
})
