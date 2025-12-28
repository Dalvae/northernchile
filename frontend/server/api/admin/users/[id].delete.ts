import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const userId = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/admin/users/${userId}`, 'Failed to delete user')
  return { status: 'success' }
})
