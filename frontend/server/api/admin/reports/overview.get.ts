import type { OverviewReport } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<OverviewReport>(event, '/api/admin/reports/overview', 'Failed to fetch reports overview')
)
