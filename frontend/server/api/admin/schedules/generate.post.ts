import { proxyPostNoBody } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPostNoBody<string>(event, '/api/admin/schedules/generate', 'Failed to generate schedules')
)
