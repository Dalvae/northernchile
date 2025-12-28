import type { TourRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const tourId = getRouterParam(event, 'id')

  try {
    return await $fetch<TourRes>(`${backendUrl}/api/tours/${tourId}`)
  } catch (error) {
    const status = (error as { response?: { status?: number } })?.response?.status || 500
    throw createError({
      statusCode: status,
      statusMessage: `Failed to fetch tour ${tourId}.`
    })
  }
})
