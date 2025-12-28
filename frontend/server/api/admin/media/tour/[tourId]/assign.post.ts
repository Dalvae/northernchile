import { proxyPost } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const tourId = getRouterParam(event, 'tourId')
  return proxyPost(event, `/api/admin/media/tour/${tourId}/assign`, 'Failed to assign media to tour')
})
