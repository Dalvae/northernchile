export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  try {
    const response = await $fetch.raw(`${backendUrl}/api/auth/logout`, {
      method: 'POST',
      headers: {
        cookie: getHeader(event, 'cookie') || ''
      }
    })

    // Forward Set-Cookie headers to clear auth_token cookie
    const setCookieHeaders = response.headers.getSetCookie()
    for (const cookie of setCookieHeaders) {
      appendResponseHeader(event, 'set-cookie', cookie)
    }

    return response._data
  } catch (error: unknown) {
    const fetchError = error as { response?: { status?: number } }
    throw createError({
      statusCode: fetchError.response?.status || 500,
      statusMessage: 'Failed to logout'
    })
  }
})
