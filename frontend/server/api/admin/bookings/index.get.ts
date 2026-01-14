import type { PageBookingRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<PageBookingRes>(event, '/api/admin/bookings', 'Failed to fetch bookings')
)
