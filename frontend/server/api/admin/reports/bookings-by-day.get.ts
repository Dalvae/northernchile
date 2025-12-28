import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<unknown>(event, '/api/admin/reports/bookings-by-day', 'Failed to fetch bookings by day')
)
