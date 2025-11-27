import type { WeatherAlert } from 'api-client'

export default defineEventHandler(async (event): Promise<WeatherAlert[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')

  try {
    const alerts = await $fetch<WeatherAlert[]>(`${backendUrl}/api/admin/alerts`, {
      headers: { Authorization: authToken || '' }
    })
    return alerts
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al obtener alertas'
    })
  }
})
