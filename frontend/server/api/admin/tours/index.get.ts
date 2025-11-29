import type { TourRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const tours = await $fetch<TourRes[]>(`${backendUrl}/api/admin/tours`, {
      headers: { Cookie: cookie },
      params: query
    })
    return tours
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to fetch admin tours'
    })
  }
})
