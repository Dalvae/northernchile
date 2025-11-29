export default defineEventHandler(async (event): Promise<Record<string, unknown>> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  try {
    const settings = await $fetch<Record<string, unknown>>(`${backendUrl}/api/admin/settings`, {
      headers: { Cookie: cookie }
    })
    return settings
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch settings'
    })
  }
})
