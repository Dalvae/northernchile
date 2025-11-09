import type { TourRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const authToken = getHeader(event, 'Authorization')

  try {
    const tours = await $fetch(`${backendUrl}/api/admin/tours`, {
      headers: { Authorization: authToken || '' }
    })
    return tours
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      message: error.message || 'Error al obtener tours de administración'
    })
  }
})
