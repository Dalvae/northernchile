import type { WeatherAlert } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<WeatherAlert[]>(event, '/api/admin/alerts/history', 'Failed to fetch alerts history')
)
