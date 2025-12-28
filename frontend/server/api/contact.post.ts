import { proxyPostPublic } from '../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPostPublic(event, '/api/contact', 'Failed to send contact message')
})
