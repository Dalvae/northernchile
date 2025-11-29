export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()

  return await $fetch(`${config.public.apiBase}/api/cart`, {
    headers: {
      cookie: getHeader(event, 'cookie') || ''
    }
  })
})
