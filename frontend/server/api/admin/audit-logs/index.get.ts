import type { AuditLogRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

// Note: Returns paginated result, but using AuditLogRes[] for simplicity
// The actual response might be wrapped in a Page object
export default defineEventHandler(event =>
  proxyGet<AuditLogRes[]>(event, '/api/admin/audit-logs', 'Failed to fetch audit logs')
)
