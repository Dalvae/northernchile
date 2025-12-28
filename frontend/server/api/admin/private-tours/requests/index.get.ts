import { proxyGet } from '../../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<unknown>(event, '/api/admin/private-tours/requests', 'Failed to fetch private tour requests')
)
