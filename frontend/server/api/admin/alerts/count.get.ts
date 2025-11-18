export default defineEventHandler(async (event): Promise<{ pending: number }> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')

  try {
    const count = await $fetch<{ pending: number }>(`${backendUrl}/api/admin/alerts/count`, {
      headers: { Authorization: authToken || '' }
    })
    return count
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al obtener conteo de alertas'
    })
  }
})
