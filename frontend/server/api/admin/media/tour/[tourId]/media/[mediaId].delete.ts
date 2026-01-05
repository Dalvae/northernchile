import { proxyDelete } from '../../../../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const tourId = getRouterParam(event, 'tourId')
  const mediaId = getRouterParam(event, 'mediaId')
  await proxyDelete(event, `/api/admin/media/tour/${tourId}/media/${mediaId}`, 'Failed to unassign media from tour')
  return { status: 'success' }
})
