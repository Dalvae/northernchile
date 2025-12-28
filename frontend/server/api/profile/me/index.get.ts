import type { UserRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGet<UserRes>(event, '/api/profile/me', 'Not authenticated')
})
