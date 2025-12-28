import { proxyPost } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPost(event, '/api/payment-sessions', 'Failed to create payment session')
})
