import type { BookingRes } from 'api-client'
import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const bookingId = getRouterParam(event, 'id')
  return proxyPut<BookingRes>(event, `/api/admin/bookings/${bookingId}`, 'Failed to update booking')
})
