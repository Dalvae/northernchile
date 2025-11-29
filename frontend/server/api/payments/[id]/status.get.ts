export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const id = getRouterParam(event, 'id')

  const cookie = getHeader(event, 'cookie') || ''

  try {
    const response = await $fetch(`${backendUrl}/api/payments/${id}/status`, {
      method: 'GET',
      headers: {
        Cookie: cookie
      }
    })
    return response
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }, data?: { message?: string }, message?: string }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || err.message || 'Failed to get payment status'
    })
  }
})
