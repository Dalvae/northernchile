import type { TourScheduleRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyGet<TourScheduleRes[]>(event, `/api/tours/${id}/schedules`, 'Failed to get tour schedules')
})
