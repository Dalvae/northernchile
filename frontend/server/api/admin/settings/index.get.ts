export default defineEventHandler(async (event): Promise<Record<string, unknown>> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')

  try {
    const settings = await $fetch<Record<string, unknown>>(`${backendUrl}/api/admin/settings`, {
      headers: { Authorization: authToken || '' }
    })
    return settings
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al obtener configuración'
    })
  }
})
