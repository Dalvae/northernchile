import type { MediaRes } from 'api-client'

export default defineEventHandler(async (event): Promise<MediaRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const mediaId = getRouterParam(event, 'id')

  try {
    const media = await $fetch<MediaRes>(`${backendUrl}/api/admin/media/${mediaId}`, {
      method: 'GET',
      headers: { Cookie: cookie }
    })
    return media
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch media'
    })
  }
})
