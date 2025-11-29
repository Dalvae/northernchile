export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const scheduleId = getRouterParam(event, 'id')

  try {
    await $fetch(`${backendUrl}/api/admin/schedules/${scheduleId}`, {
      method: 'DELETE',
      headers: { 'Cookie': cookie }
    })
    return { status: 'success' }
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to delete schedule'
    })
  }
})
