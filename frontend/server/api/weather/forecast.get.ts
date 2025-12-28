import { proxyGetPublic } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGetPublic(event, '/api/weather/forecast', 'Failed to get weather forecast')
})
