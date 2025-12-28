import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<Record<string, object>>(event, '/api/admin/audit-logs/stats', 'Failed to fetch audit logs stats')
)
