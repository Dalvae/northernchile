import { proxyDelete } from '../../utils/apiProxy'

export default defineEventHandler(async (event) => {
  const id = getRouterParam(event, 'id')
  await proxyDelete(event, `/api/bookings/${id}`, 'Failed to delete booking')
})
