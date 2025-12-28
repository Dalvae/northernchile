import type { UserRes } from 'api-client'
import { proxyPost } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPost<UserRes>(event, '/api/admin/users', 'Failed to create user')
)
