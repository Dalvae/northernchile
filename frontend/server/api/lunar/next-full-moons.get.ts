export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const query = getQuery(event)

  try {
    const response = await $fetch(`${backendUrl}/api/lunar/next-full-moons`, {
      method: 'GET',
      query: query
    })
    return response
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }, data?: { message?: string }, message?: string }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || err.message || 'Failed to get next full moons'
    })
  }
})
