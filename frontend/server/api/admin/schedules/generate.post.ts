import { proxyPostNoBody } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPostNoBody<unknown>(event, '/api/admin/schedules/generate', 'Failed to generate schedules')
)
