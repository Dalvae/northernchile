import type { SavedParticipantRes } from 'api-client'
import { proxyPut } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  const id = getRouterParam(event, 'id')
  return proxyPut<SavedParticipantRes>(event, `/api/profile/participants/${id}`, 'Failed to update participant')
})
