export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  try {
    const response = await $fetch(`${backendUrl}/api/profile/me`, {
      method: 'GET',
      headers: {
        cookie: getHeader(event, 'cookie') || ''
      }
    })

    return response
  } catch (error: unknown) {
    const fetchError = error as { response?: { status?: number } }
    throw createError({
      statusCode: fetchError.response?.status || 401,
      statusMessage: 'Not authenticated'
    })
  }
})
