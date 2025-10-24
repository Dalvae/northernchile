export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.backendApiUrl

  // 1. Leer el token de la petición original del cliente
  const authToken = getHeader(event, 'Authorization')

  // 2. Leer el cuerpo de la petición (el payload de la reserva)
  const body = await readBody(event)

  try {
    const createdBooking = await $fetch(`${backendUrl}/api/bookings`, {
      method: 'POST',
      body: body,
      headers: {
        // 3. Reenviar el token de autorización al backend de Spring Boot
        'Authorization': authToken || '',
        'Content-Type': 'application/json'
      }
    })
    return createdBooking
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      statusMessage: 'Failed to create booking.'
    })
  }
})
