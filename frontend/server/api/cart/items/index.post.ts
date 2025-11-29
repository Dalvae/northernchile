export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig()
  const body = await readBody(event)

  const response = await $fetch.raw(`${config.public.apiBase}/api/cart/items`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'cookie': getHeader(event, 'cookie') || ''
    },
    body
  })

  // Forward Set-Cookie headers from backend to client
  const setCookieHeader = response.headers.get('set-cookie')
  if (setCookieHeader) {
    appendResponseHeader(event, 'set-cookie', setCookieHeader)
  }

  return response._data
})
