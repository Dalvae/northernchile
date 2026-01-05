import { proxyDelete } from '../../../../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const scheduleId = getRouterParam(event, 'scheduleId')
  const mediaId = getRouterParam(event, 'mediaId')
  await proxyDelete(event, `/api/admin/media/schedule/${scheduleId}/media/${mediaId}`, 'Failed to unassign media from schedule')
  return { status: 'success' }
})
