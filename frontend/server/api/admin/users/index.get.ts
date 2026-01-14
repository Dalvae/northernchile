import type { UserRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

// Backend returns Page<UserRes>, we extract content for backward compatibility
interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
}

export default defineEventHandler(async (event) => {
  const page = await proxyGet<PageResponse<UserRes>>(event, '/api/admin/users', 'Failed to fetch users')
  return page.content
})
