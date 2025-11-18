export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')

  try {
    const alerts = await $fetch(`${backendUrl}/api/admin/alerts`, {
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
