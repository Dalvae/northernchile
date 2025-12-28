import { proxyGetPublic } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGetPublic(event, '/api/calendar/data', 'Failed to get calendar data')
})
