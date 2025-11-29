export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const scheduleId = getRouterParam(event, 'scheduleId')

  try {
    const gallery = await $fetch(`${backendUrl}/api/admin/media/schedule/${scheduleId}/gallery`, {
      headers: { Cookie: cookie }
    })
    return gallery
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch schedule gallery'
    })
  }
})
