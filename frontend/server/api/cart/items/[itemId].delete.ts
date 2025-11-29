export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const itemId = getRouterParam(event, 'itemId')

  return await $fetch(`${config.public.apiBase}/api/cart/items/${itemId}`, {
    method: 'DELETE',
    headers: {
      cookie: getHeader(event, 'cookie') || ''
    }
  })
})
