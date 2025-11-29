import type { TourScheduleRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourScheduleRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const schedules = await $fetch<TourScheduleRes[]>(`${backendUrl}/api/admin/schedules`, {
      headers: { Cookie: cookie },
      query
    })
    return schedules
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch schedules'
    })
  }
})
