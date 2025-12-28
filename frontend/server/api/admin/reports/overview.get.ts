import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<unknown>(event, '/api/admin/reports/overview', 'Failed to fetch reports overview')
)
