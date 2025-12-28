import { proxyPost } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const scheduleId = getRouterParam(event, 'scheduleId')
  return proxyPost(event, `/api/admin/media/schedule/${scheduleId}/assign`, 'Failed to assign media to schedule')
})
