import type { SavedParticipantRes } from 'api-client'
import { proxyGet } from '../../../utils/apiProxy'

export default defineEventHandler((event) => {
  return proxyGet<SavedParticipantRes>(event, '/api/profile/participants/self', 'Self participant not found')
})
