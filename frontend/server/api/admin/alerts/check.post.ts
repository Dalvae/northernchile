import type { WeatherAlert } from 'api-client'
import { proxyPostNoBody } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyPostNoBody<WeatherAlert[]>(event, '/api/admin/alerts/check', 'Failed to check alerts')
)
