import type { WeatherAlert } from 'api-client'

export default defineEventHandler(async (event): Promise<WeatherAlert[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const alerts = await $fetch<WeatherAlert[]>(`${backendUrl}/api/admin/alerts`, {
      headers: { Cookie: cookie },
      params: query
    })
    return alerts
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Error al obtener alertas'
    })
  }
})
