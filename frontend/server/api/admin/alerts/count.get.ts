import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<{ pending: number }>(event, '/api/admin/alerts/count', 'Failed to fetch alerts count')
)
