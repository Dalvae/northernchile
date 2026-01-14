import { proxyPost } from '../../../../utils/apiProxy'

interface ResolveAlertResponse {
  message: string
}

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyPost<ResolveAlertResponse>(event, `/api/admin/alerts/${id}/resolve`, 'Failed to resolve alert')
})
