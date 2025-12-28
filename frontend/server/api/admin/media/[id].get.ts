import type { MediaRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const mediaId = getRouterParam(event, 'id')
  return proxyGet<MediaRes>(event, `/api/admin/media/${mediaId}`, 'Failed to fetch media')
})
