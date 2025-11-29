export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  try {
    const result: unknown = await $fetch(`${backendUrl}/api/admin/private-tours/requests`, {
      headers: { 'Cookie': cookie }
    })
    return result
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch private tour requests'
    })
  }
})
