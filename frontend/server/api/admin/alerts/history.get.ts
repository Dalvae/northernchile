import type { AlertHistoryRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<AlertHistoryRes>(event, '/api/admin/alerts/history', 'Failed to fetch alerts history')
)
