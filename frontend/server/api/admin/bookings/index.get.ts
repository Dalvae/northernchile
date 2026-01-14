import type { BookingRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

// Backend returns Page<BookingRes>, we extract content for backward compatibility
interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
}

export default defineEventHandler(async (event) => {
  const page = await proxyGet<PageResponse<BookingRes>>(event, '/api/admin/bookings', 'Failed to fetch bookings')
  return page.content
})
