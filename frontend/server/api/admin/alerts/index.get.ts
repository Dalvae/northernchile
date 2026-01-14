import type { WeatherAlertRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler(event =>
  proxyGet<WeatherAlertRes[]>(event, '/api/admin/alerts', 'Error al obtener alertas')
)
