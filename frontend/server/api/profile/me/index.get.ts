import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGet(event, '/api/profile/me', 'Not authenticated')
})
