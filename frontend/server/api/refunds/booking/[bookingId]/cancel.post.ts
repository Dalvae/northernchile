import { proxyPostNoBody } from '../../../../utils/apiProxy'

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
  return await proxyPostNoBody<RefundResponse>(
    event,
    `/api/refunds/booking/${bookingId}/cancel`,
    'Failed to process refund'
  )
})
