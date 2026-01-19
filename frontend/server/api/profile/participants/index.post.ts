import type { SavedParticipantRes } from 'api-client'
import { proxyPost } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyPost<SavedParticipantRes>(event, '/api/profile/participants', 'Failed to create participant')
})
