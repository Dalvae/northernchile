export default defineEventHandler(async (event): Promise<Record<string, unknown>> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  try {
    const updatedSettings = await $fetch<Record<string, unknown>>(`${backendUrl}/api/admin/settings`, {
      method: 'PATCH',
      headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
      body
    })
    return updatedSettings
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to update settings'
    })
  }
})
