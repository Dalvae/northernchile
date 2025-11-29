import type { UserRes } from 'api-client'

export default defineEventHandler(async (event): Promise<UserRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const userId = getRouterParam(event, 'id')

  try {
    const user = await $fetch<UserRes>(`${backendUrl}/api/admin/users/${userId}`, {
      headers: { Cookie: cookie }
    })
    return user
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch user'
    })
  }
})
