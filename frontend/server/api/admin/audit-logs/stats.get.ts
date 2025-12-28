import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<unknown>(event, '/api/admin/audit-logs/stats', 'Failed to fetch audit logs stats')
)
