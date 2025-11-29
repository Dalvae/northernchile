export default defineEventHandler(async (event): Promise<{ pending: number }> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  try {
    const count = await $fetch<{ pending: number }>(`${backendUrl}/api/admin/alerts/count`, {
      headers: { Cookie: cookie }
    })
    return count
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch alerts count'
    })
  }
})
