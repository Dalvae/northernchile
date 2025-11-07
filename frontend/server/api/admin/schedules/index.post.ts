export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const body = await readBody(event)

  try {
    const newSchedule = await $fetch(`${backendUrl}/api/tour-schedules`, {
      method: 'POST',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body: body
    })
    return newSchedule
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al crear horario de tour'
    })
  }
})
