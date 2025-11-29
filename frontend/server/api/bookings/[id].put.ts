export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const id = getRouterParam(event, 'id')

  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  try {
    const response = await $fetch(`${backendUrl}/api/bookings/${id}`, {
      method: 'PUT' as const,
      body: body,
      headers: {
        'Cookie': cookie,
        'Content-Type': 'application/json'
      }
    })
    return response
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }, data?: { message?: string }, message?: string }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || err.message || 'Failed to update booking'
    })
  }
})
