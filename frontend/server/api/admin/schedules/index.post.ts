import type { TourScheduleRes } from 'api-client'
import { proxyPost } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPost<TourScheduleRes>(event, '/api/admin/schedules', 'Failed to create schedule')
)
