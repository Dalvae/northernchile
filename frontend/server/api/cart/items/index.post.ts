export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const body = await readBody(event)

  return await $fetch(`${config.public.apiBase}/api/cart/items`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      cookie: getHeader(event, 'cookie') || ''
    },
    body
  })
})
