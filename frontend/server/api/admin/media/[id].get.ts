export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const mediaId = getRouterParam(event, 'id')

  try {
    const media = await $fetch(`${backendUrl}/api/admin/media/${mediaId}`, {
      method: 'GET',
      headers: { Authorization: authToken || '' }
    })
    return media
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al obtener media'
    })
  }
})
