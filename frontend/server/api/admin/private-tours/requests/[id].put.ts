import { proxyPut } from '../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyPut<unknown>(event, `/api/admin/private-tours/requests/${id}`, 'Failed to update private tour request')
})
