import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) =>
  proxyGet<unknown>(event, '/api/admin/alerts/history', 'Failed to fetch alerts history')
)
