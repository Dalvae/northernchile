export default defineEventHandler(async (event) => {
  // Obtenemos la URL secreta del backend desde la configuración
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  try {
    // El servidor de Nuxt hace la llamada al servidor de Spring Boot
    // Usamos $fetch, que es la utilidad de Nuxt para hacer peticiones http
    const tours = await $fetch(`${backendUrl}/api/tours`)
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
