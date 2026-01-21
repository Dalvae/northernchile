import type { TourScheduleRes } from 'api-client'
import { proxyGet } from '~/server/utils/proxy'

export default defineEventHandler(async (event) => {
  return proxyGet<TourScheduleRes[]>(event, '/api/tours/schedules/all', 'Failed to get all tour schedules')
})
