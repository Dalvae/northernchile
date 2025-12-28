import type { TopTourReport } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<TopTourReport[]>(event, '/api/admin/reports/top-tours', 'Failed to fetch top tours')
)
