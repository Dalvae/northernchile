import type { UserRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<UserRes[]>(event, '/api/admin/users', 'Failed to fetch users')
)
