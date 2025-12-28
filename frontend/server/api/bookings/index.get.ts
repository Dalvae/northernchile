import { proxyGet } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGet(event, '/api/bookings', 'Failed to get bookings')
})
