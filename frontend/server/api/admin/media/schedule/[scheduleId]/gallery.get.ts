import type { MediaRes } from 'api-client'
import { proxyGet } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const scheduleId = getRouterParam(event, 'scheduleId')
  return proxyGet<MediaRes[]>(event, `/api/admin/media/schedule/${scheduleId}/gallery`, 'Failed to fetch schedule gallery')
})
