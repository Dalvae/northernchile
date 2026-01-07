/**
 * DELETE /api/cart - Clear the entire cart
 * Called after successful payment to remove all items
 */
export default defineEventHandler(async (event): Promise<void> => {
  const config = useRuntimeConfig()

  const response = await $fetch.raw(`${config.public.apiBase}/api/cart`, {
    method: 'DELETE',
    headers: {
      cookie: getHeader(event, 'cookie') || ''
    }
  })

  // Forward Set-Cookie headers from backend to client (clears cartId cookie)
  const setCookieHeader = response.headers.get('set-cookie')
  if (setCookieHeader) {
    appendResponseHeader(event, 'set-cookie', setCookieHeader)
  }

  // Return 204 No Content
  setResponseStatus(event, 204)
})
