interface RefundResponse {
  bookingId: string
  provider: string
  providerRefundId: string
  refundAmount: number
  status: string
  message: string
}

export default defineEventHandler(async (event) => {
  const bookingId = getRouterParam(event, 'bookingId')
  const query = getQuery(event)
  const adminOverride = query.adminOverride === 'true'

  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  try {
    const result = await $fetch<RefundResponse>(`${backendUrl}/api/refunds/booking/${bookingId}?adminOverride=${adminOverride}`, {
      method: 'POST',
      headers: { Cookie: cookie }
    })
    return result
  } catch (error) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to process refund'
    })
  }
})
