export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  console.log('[payments/init] Forwarding request with cookie:', cookie ? 'present' : 'missing')

  try {
    const response = await $fetch(`${backendUrl}/api/payments/init`, {
      method: 'POST',
      body: body,
      headers: {
        'Cookie': cookie,
        'Content-Type': 'application/json'
      }
    })
    return response
  } catch (error: unknown) {
    console.error('[payments/init] Error:', error)
    const err = error as { statusCode?: number, statusMessage?: string, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.statusMessage || err.message || 'Failed to initialize payment'
    })
  }
})
