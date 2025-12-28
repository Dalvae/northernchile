import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyGet(event, `/api/payments/${id}/status`, 'Failed to get payment status')
})
