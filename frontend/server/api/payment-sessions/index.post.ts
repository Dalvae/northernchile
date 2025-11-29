export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  try {
    const response = await $fetch(`${backendUrl}/api/payment-sessions`, {
      method: 'POST',
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
      statusMessage: err.data?.message || err.message || 'Failed to create payment session'
    })
  }
})
