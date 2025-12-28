import type { BookingRes } from 'api-client'
import { proxyGet } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGet<BookingRes[]>(event, '/api/bookings', 'Failed to get bookings')
})
