import type { TourScheduleRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<TourScheduleRes[]>(event, '/api/admin/schedules', 'Failed to fetch schedules')
)
