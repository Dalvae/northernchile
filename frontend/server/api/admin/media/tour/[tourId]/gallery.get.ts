import type { MediaRes } from 'api-client'
import { proxyGet } from '../../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const tourId = getRouterParam(event, 'tourId')
  return proxyGet<MediaRes[]>(event, `/api/admin/media/tour/${tourId}/gallery`, 'Failed to fetch tour gallery')
})
