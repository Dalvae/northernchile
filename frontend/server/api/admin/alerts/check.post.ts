import { proxyPostNoBody } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPostNoBody<unknown>(event, '/api/admin/alerts/check', 'Failed to check alerts')
)
