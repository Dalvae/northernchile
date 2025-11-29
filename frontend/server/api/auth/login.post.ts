export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const body = await readBody(event)

  try {
    const response = await $fetch.raw(`${backendUrl}/api/auth/login`, {
      method: 'POST',
      body,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    // Forward Set-Cookie headers from backend to client
    // This ensures auth_token cookie is set on the frontend domain
    const setCookieHeaders = response.headers.getSetCookie()
    for (const cookie of setCookieHeaders) {
      appendResponseHeader(event, 'set-cookie', cookie)
    }

    return response._data
  } catch (error: unknown) {
    const fetchError = error as { response?: { status?: number } }
    throw createError({
      statusCode: fetchError.response?.status || 401,
      statusMessage: 'Invalid credentials'
    })
  }
})
