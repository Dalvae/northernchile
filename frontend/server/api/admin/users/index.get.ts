import type { PageUserRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<PageUserRes>(event, '/api/admin/users', 'Failed to fetch users')
)
