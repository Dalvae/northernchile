export default defineEventHandler(async (event): Promise<Record<string, unknown>> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const body = await readBody(event)

  try {
    const updatedSettings = await $fetch<Record<string, unknown>>(`${backendUrl}/api/admin/settings`, {
      method: 'PATCH',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body
    })
    return updatedSettings
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al actualizar configuración'
    })
  }
})
