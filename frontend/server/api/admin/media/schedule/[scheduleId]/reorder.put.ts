import { proxyPut } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const scheduleId = getRouterParam(event, 'scheduleId')
  return proxyPut(event, `/api/admin/media/schedule/${scheduleId}/reorder`, 'Failed to reorder schedule gallery')
})
