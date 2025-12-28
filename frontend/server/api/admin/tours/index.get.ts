import type { TourRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<TourRes[]>(event, '/api/admin/tours', 'Failed to fetch admin tours')
)
