import type { TourScheduleRes } from 'api-client'
import { proxyPatch } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const scheduleId = getRouterParam(event, 'id')
  return proxyPatch<TourScheduleRes>(event, `/api/admin/schedules/${scheduleId}`, 'Failed to update schedule')
})
