import { proxyDelete } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyDelete(event, `/api/profile/participants/${id}`, 'Failed to delete participant')
})
