import { proxyPutNoBody } from '../../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const tourId = getRouterParam(event, 'tourId')
  const mediaId = getRouterParam(event, 'mediaId')
  return proxyPutNoBody(event, `/api/admin/media/tour/${tourId}/featured/${mediaId}`, 'Failed to toggle featured image')
})
