import type { UserRes } from 'api-client'

export default defineEventHandler(async (event): Promise<UserRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const users = await $fetch<UserRes[]>(`${backendUrl}/api/admin/users`, {
      headers: { 'Cookie': cookie },
      query
    })
    return users
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch users'
    })
  }
})
