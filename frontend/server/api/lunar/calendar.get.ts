import { proxyGetPublic } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGetPublic(event, '/api/lunar/calendar', 'Failed to get lunar calendar')
})
