import type { PageMediaRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<PageMediaRes>(event, '/api/admin/media', 'Failed to fetch media')
)
