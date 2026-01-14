import { proxyPostNoBody } from '../../../utils/apiProxy'

interface CheckAlertsResponse {
  message: string
  pendingAlerts: number
}

export default defineEventHandler(event =>
  proxyPostNoBody<CheckAlertsResponse>(event, '/api/admin/alerts/check', 'Failed to check alerts')
)
