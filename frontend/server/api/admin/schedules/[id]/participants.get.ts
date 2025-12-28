import type { ParticipantRes } from 'api-client'
import { proxyGet } from '../../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyGet<ParticipantRes[]>(event, `/api/admin/schedules/${id}/participants`, 'Failed to fetch participants')
})
