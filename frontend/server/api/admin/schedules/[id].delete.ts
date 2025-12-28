import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const scheduleId = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/admin/schedules/${scheduleId}`, 'Failed to delete schedule')
  return { status: 'success' }
})
