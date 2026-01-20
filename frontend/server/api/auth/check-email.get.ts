export default defineEventHandler(async (event) => {
  const query = getQuery(event)
  const config = useRuntimeConfig()

  if (!query.email) {
    throw createError({
      statusCode: 400,
      message: 'Email is required'
    })
  }

  return await $fetch<{ exists: boolean }>(`${config.apiBaseUrl}/auth/check-email`, {
    params: { email: query.email }
  })
})
