export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const scheduleId = getRouterParam(event, 'id')

  try {
    await $fetch(`${backendUrl}/api/admin/schedules/${scheduleId}`, {
      method: 'DELETE',
      headers: { Authorization: authToken || '' }
    })
    return { status: 'success', message: 'Horario de tour eliminado correctamente' }
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al eliminar horario de tour'
    })
  }
})
