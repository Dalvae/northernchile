import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const tourId = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/admin/tours/${tourId}`, 'Failed to delete tour')
  return { status: 'success' }
})
