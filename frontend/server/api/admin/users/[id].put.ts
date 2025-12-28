import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const userId = getRouterParam(event, 'id')
  return proxyPut<unknown>(event, `/api/admin/users/${userId}`, 'Failed to update user')
})
