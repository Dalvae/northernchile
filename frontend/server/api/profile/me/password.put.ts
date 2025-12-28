import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPut(event, '/api/profile/me/password', 'Failed to update password')
})
