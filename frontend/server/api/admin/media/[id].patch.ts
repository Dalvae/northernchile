import type { MediaRes } from 'api-client'

export default defineEventHandler(async (event): Promise<MediaRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const mediaId = getRouterParam(event, 'id')
  const body = await readBody(event)

  try {
    const updatedMedia = await $fetch<MediaRes>(`${backendUrl}/api/admin/media/${mediaId}`, {
      method: 'PATCH',
      headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
      body
    })
    return updatedMedia
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to update media'
    })
  }
})
