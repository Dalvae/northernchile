import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const bookingId = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/admin/bookings/${bookingId}`, 'Failed to delete booking')
  return { status: 'success' }
})
