import { proxyGet } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const scheduleId = getRouterParam(event, 'scheduleId')
  return proxyGet(event, `/api/admin/media/schedule/${scheduleId}/gallery`, 'Failed to fetch schedule gallery')
})
