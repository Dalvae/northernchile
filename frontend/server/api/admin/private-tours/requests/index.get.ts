import type { PrivateTourRequest } from 'api-client'
import { proxyGet } from '../../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<PrivateTourRequest[]>(event, '/api/admin/private-tours/requests', 'Failed to fetch private tour requests')
)
