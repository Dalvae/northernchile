import { proxyPost } from '../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPost(event, '/api/private-tours/requests', 'Failed to create private tour request')
})
