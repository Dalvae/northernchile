import type { BookingRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<BookingRes[]>(event, '/api/admin/bookings', 'Failed to fetch bookings')
)
