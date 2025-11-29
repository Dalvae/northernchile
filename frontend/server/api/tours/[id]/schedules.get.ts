export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const id = getRouterParam(event, 'id')

  try {
    const response = await $fetch(`${backendUrl}/api/tours/${id}/schedules`, {
      method: 'GET'
    })
    return response
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }, data?: { message?: string }, message?: string }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || err.message || 'Failed to get tour schedules'
    })
  }
})
