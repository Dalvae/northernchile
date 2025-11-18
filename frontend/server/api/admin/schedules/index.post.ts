import type { TourScheduleRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourScheduleRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const body = await readBody(event)

  try {
    const newSchedule = await $fetch<TourScheduleRes>(`${backendUrl}/api/admin/schedules`, {
      method: 'POST',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body
    })
    return newSchedule
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al crear horario de tour'
    })
  }
})
