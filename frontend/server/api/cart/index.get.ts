export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig()

  const response = await $fetch.raw(`${config.public.apiBase}/api/cart`, {
    headers: {
      cookie: getHeader(event, 'cookie') || ''
    }
  })

  // Forward Set-Cookie headers from backend to client
  const setCookieHeader = response.headers.get('set-cookie')
  if (setCookieHeader) {
    appendResponseHeader(event, 'set-cookie', setCookieHeader)
  }

  return response._data
})
