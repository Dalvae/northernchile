import type { UserRes } from 'api-client'

export default defineEventHandler(async (event): Promise<UserRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const userId = getRouterParam(event, 'id')

  try {
    const user = await $fetch<UserRes>(`${backendUrl}/api/users/${userId}`, {
      headers: { Authorization: authToken || '' }
    })
    return user
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener usuario'
    })
  }
})
