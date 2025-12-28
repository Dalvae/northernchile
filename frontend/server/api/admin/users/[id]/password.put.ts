import { proxyPut } from '../../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const userId = getRouterParam(event, 'id')
  await proxyPut(event, `/api/admin/users/${userId}/password`, 'Failed to reset password')
  return { status: 'success' }
})
