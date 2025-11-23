export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const mediaId = getRouterParam(event, 'id')

  try {
    await $fetch(`${backendUrl}/api/admin/media/${mediaId}`, {
      method: 'DELETE',
      headers: { Authorization: authToken || '' }
    })
    return { status: 'success', message: 'Media eliminado correctamente' }
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al eliminar media'
    })
  }
})
