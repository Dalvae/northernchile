import { proxyGetPublic } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGetPublic(event, '/api/lunar/next-full-moons', 'Failed to get next full moons')
})
