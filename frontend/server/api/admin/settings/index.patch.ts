import { proxyPatch } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPatch<Record<string, unknown>>(event, '/api/admin/settings', 'Failed to update settings')
)
