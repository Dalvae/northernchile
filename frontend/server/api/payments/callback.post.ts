/**
 * POST endpoint to receive Transbank callback
 * Transbank sends token_ws via POST body
 * We confirm the payment here and redirect to callback page with result
 */
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const body = await readBody(event)
  
  // Transbank sends token_ws in POST body
  const tokenWs = body?.token_ws
  
  if (!tokenWs) {
    return sendRedirect(event, '/payment/callback?status=error&message=No token received', 302)
  }
  
  try {
    // Confirm payment with backend
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
    const message = err.data?.message || err.message || 'Payment confirmation failed'
    return sendRedirect(event, `/payment/callback?status=error&message=${encodeURIComponent(message)}`, 302)
  }
})
