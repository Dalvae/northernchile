import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<Record<string, unknown>>(event, '/api/admin/settings', 'Failed to fetch settings')
)
