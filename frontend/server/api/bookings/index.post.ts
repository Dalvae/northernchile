import { proxyPost } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPost(event, '/api/bookings', 'Failed to create booking')
})
