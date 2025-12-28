import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const mediaId = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/admin/media/${mediaId}`, 'Failed to delete media')
  return { status: 'success' }
})
