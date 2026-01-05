export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const query = getQuery(event)

  try {
    // Use new payment-sessions endpoint for confirmation
    const response = await $fetch(`${backendUrl}/api/payment-sessions/confirm`, {
      query: query
    })
    return response
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }, data?: { message?: string }, message?: string }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || err.message || 'Failed to confirm payment'
    })
  }
})
