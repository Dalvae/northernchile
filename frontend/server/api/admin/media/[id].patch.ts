import type { MediaRes } from 'api-client'
import { proxyPatch } from '../../../utils/apiProxy'

export default defineEventHandler((event): Promise<MediaRes> => {
  const mediaId = getRouterParam(event, 'id')
  return proxyPatch<MediaRes>(event, `/api/admin/media/${mediaId}`, 'Failed to update media')
})
