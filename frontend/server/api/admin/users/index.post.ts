import { proxyPost } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyPost<unknown>(event, '/api/admin/users', 'Failed to create user')
)
