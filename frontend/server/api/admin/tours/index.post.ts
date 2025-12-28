import type { TourRes } from 'api-client'
import { proxyPost } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyPost<TourRes>(event, '/api/admin/tours', 'Failed to create tour')
)
