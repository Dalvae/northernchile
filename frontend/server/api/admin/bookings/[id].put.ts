import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const bookingId = getRouterParam(event, 'id')
  return proxyPut<unknown>(event, `/api/admin/bookings/${bookingId}`, 'Failed to update booking')
})
