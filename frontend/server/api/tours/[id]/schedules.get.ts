import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyGet(event, `/api/tours/${id}/schedules`, 'Failed to get tour schedules')
})
