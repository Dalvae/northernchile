import type { WeatherAlert } from 'api-client'
import { proxyPost } from '../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyPost<WeatherAlert>(event, `/api/admin/alerts/${id}/resolve`, 'Failed to resolve alert')
})
