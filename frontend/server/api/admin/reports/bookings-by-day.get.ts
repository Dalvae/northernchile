import type { BookingsByDayReport } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<BookingsByDayReport[]>(event, '/api/admin/reports/bookings-by-day', 'Failed to fetch bookings by day')
)
