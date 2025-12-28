import type { BookingRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const bookingId = getRouterParam(event, 'id')
  return proxyGet<BookingRes>(event, `/api/admin/bookings/${bookingId}`, 'Failed to fetch booking')
})
