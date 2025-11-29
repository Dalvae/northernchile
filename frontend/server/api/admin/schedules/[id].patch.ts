import type { TourScheduleRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourScheduleRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const scheduleId = getRouterParam(event, 'id')
  const body = await readBody(event)

  try {
    const updatedSchedule = await $fetch<TourScheduleRes>(`${backendUrl}/api/admin/schedules/${scheduleId}`, {
      method: 'PATCH',
      headers: { 'Cookie': cookie, 'Content-Type': 'application/json' },
      body
    })
    return updatedSchedule
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to update schedule'
    })
  }
})
