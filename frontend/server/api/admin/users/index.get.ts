import type { UserRes } from 'api-client'

export default defineEventHandler(async (event): Promise<UserRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const query = getQuery(event)

  try {
    const users = await $fetch<UserRes[]>(`${backendUrl}/api/users`, {
      headers: { Authorization: authToken || '' },
      query: query
    })
    return users
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener usuarios'
    })
  }
})
