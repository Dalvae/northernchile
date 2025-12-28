import { proxyPut } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyPut(event, `/api/bookings/${id}`, 'Failed to update booking')
})
