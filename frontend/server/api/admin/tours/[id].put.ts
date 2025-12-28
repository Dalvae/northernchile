import type { TourRes } from 'api-client'
import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const tourId = getRouterParam(event, 'id')
  return proxyPut<TourRes>(event, `/api/admin/tours/${tourId}`, 'Failed to update tour')
})
