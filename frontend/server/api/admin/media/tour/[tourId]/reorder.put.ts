import { proxyPut } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const tourId = getRouterParam(event, 'tourId')
  return proxyPut(event, `/api/admin/media/tour/${tourId}/reorder`, 'Failed to reorder tour gallery')
})
