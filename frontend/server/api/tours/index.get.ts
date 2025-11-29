import type { TourRes } from 'api-client'

export default defineEventHandler(async (event): Promise<TourRes[]> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const query = getQuery(event)

  try {
    const tours = await $fetch(`${backendUrl}/api/tours`, {
      params: query
    })
    return tours
  } catch (error) {
    // Si la API de Spring Boot devuelve un error, lo reenviamos al frontend
    // para que useFetch pueda manejarlo correctamente.
    console.error('Error fetching tours from backend:', error)
    throw createError({
      statusCode: 500,
      statusMessage: 'Failed to fetch tours from the backend API.'
    })
  }
})
