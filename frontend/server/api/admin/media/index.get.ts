import type { PageMediaRes } from 'api-client'

export default defineEventHandler(async (event): Promise<PageMediaRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const response = await $fetch<PageMediaRes>(`${backendUrl}/api/admin/media`, {
      method: 'GET',
      params: query,
      headers: { 'Cookie': cookie }
    })
    return response
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch media'
    })
  }
})
