import type { AuditLog } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

// Note: Returns paginated result, but using AuditLog[] for simplicity
// The actual response might be wrapped in a Page object
export default defineEventHandler(event =>
  proxyGet<AuditLog[]>(event, '/api/admin/audit-logs', 'Failed to fetch audit logs')
)
