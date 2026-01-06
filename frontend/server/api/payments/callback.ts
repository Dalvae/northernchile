/**
 * Endpoint to receive Transbank callback
 *
 * Transbank sends different parameters depending on the result:
 * - token_ws: Payment completed (success or failure) - needs to be confirmed with commit()
 * - TBK_TOKEN + TBK_ORDEN_COMPRA + TBK_ID_SESION: User cancelled or timeout
 * - No token: User closed the browser during payment
 */
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const method = event.method

  // Get parameters from body (POST) or query (GET)
  let tokenWs: string | undefined
  let tbkToken: string | undefined
  let tbkOrdenCompra: string | undefined
  let tbkIdSesion: string | undefined

  if (method === 'POST') {
    const body = await readBody(event)
    tokenWs = body?.token_ws
    tbkToken = body?.TBK_TOKEN
    tbkOrdenCompra = body?.TBK_ORDEN_COMPRA
    tbkIdSesion = body?.TBK_ID_SESION
  } else {
    const query = getQuery(event)
    tokenWs = query.token_ws as string
    tbkToken = query.TBK_TOKEN as string
    tbkOrdenCompra = query.TBK_ORDEN_COMPRA as string
    tbkIdSesion = query.TBK_ID_SESION as string
  }

  // Case 1: User cancelled or timeout - TBK_TOKEN is sent
  // Forward to backend to log and handle properly (don't call commit!)
  if (tbkToken) {
    console.log('Transbank payment cancelled - TBK_TOKEN:', tbkToken, 'Session:', tbkIdSesion, 'Order:', tbkOrdenCompra)
    
    try {
      // Call backend to mark session as cancelled and log the values
      await $fetch(`${backendUrl}/api/payment-sessions/confirm`, {
        method: 'GET',
        query: { 
          TBK_TOKEN: tbkToken,
          TBK_ID_SESION: tbkIdSesion,
          TBK_ORDEN_COMPRA: tbkOrdenCompra
        }
      })
    } catch (error) {
      // Backend handles cancellation - ignore errors here
      console.log('Backend handled Transbank abort')
    }
    
    return sendRedirect(event, '/payment/callback?status=cancelled&message=Pago cancelado por el usuario', 302)
  }

  // Case 2: No token at all - user closed the browser
  if (!tokenWs) {
    console.log('Transbank callback received without any token')
    return sendRedirect(event, '/payment/callback?status=error&message=No se recibió información del pago', 302)
  }

  // Case 3: Normal flow - token_ws received, confirm with backend
  try {
    const result = await $fetch<{
      sessionId: string
      status: string
      bookingIds?: string[]
      paymentId?: string
      message?: string
    }>(`${backendUrl}/api/payment-sessions/confirm`, {
      method: 'GET',
      query: { token_ws: tokenWs }
    })

    // Build redirect URL with result
    const params = new URLSearchParams()
    params.set('status', result.status === 'COMPLETED' ? 'success' : 'error')
    if (result.paymentId) params.set('paymentId', result.paymentId)
    if (result.sessionId) params.set('sessionId', result.sessionId)
    if (result.bookingIds?.length) params.set('bookingIds', result.bookingIds.join(','))
    if (result.message) params.set('message', result.message)

    return sendRedirect(event, `/payment/callback?${params.toString()}`, 302)
  } catch (error: unknown) {
    console.error('Error confirming Transbank payment:', error)
    const err = error as { data?: { message?: string }, message?: string }

    // Check if it's an aborted transaction
    const errorMessage = err.data?.message || err.message || ''
    if (errorMessage.includes('aborted')) {
      return sendRedirect(event, '/payment/callback?status=cancelled&message=El pago fue cancelado o expiró', 302)
    }

    const message = errorMessage || 'Error al confirmar el pago'
    return sendRedirect(event, `/payment/callback?status=error&message=${encodeURIComponent(message)}`, 302)
  }
})
