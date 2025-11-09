import type { TourRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')
  const body = await readBody(event)

  try {
    const newTour = await $fetch<TourRes>(`${backendUrl}/api/tours`, {
      method: 'POST' as any,
      headers: {
        'Authorization': authToken || '',
        'Content-Type': 'application/json'
      },
      body: body
    })
    return newTour
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al crear tour'
    })
  }
})
