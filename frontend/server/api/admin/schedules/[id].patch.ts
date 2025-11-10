import type { TourScheduleRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourScheduleRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const scheduleId = getRouterParam(event, 'id')
  const body = await readBody(event)

  try {
    const updatedSchedule = await $fetch<TourScheduleRes>(`${backendUrl}/api/admin/schedules/${scheduleId}`, {
      method: 'PATCH',
      headers: {
        Authorization: authToken || '',
        'Content-Type': 'application/json'
      },
      body
    })

    return updatedSchedule
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar horario de tour'
    })
  }
})
