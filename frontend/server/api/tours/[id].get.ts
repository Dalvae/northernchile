export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.backendApiUrl

  // Obtenemos el 'id' de la URL, por ejemplo: /api/tours/abc-123
  const tourId = getRouterParam(event, 'id')

  try {
    const tour = await $fetch(`${backendUrl}/api/tours/${tourId}`)
    return tour
  } catch (error: any) {
    // Reenviamos el código de estado del backend (ej: un 404 si el tour no existe)
    throw createError({
      statusCode: error.response?.status || 500,
      statusMessage: `Failed to fetch tour ${tourId}.`
    })
  }
})
