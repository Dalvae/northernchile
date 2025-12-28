import type { UserRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const userId = getRouterParam(event, 'id')
  return proxyGet<UserRes>(event, `/api/admin/users/${userId}`, 'Failed to fetch user')
})
