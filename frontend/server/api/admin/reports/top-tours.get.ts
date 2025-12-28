import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<unknown>(event, '/api/admin/reports/top-tours', 'Failed to fetch top tours')
)
