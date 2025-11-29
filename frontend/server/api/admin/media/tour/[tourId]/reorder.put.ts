export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const tourId = getRouterParam(event, 'tourId')
  const body = await readBody(event)

  try {
    const result = await $fetch(`${backendUrl}/api/admin/media/tour/${tourId}/reorder`, {
      method: 'PUT',
      headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
      body
    })
    return result
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to reorder tour gallery'
    })
  }
})
