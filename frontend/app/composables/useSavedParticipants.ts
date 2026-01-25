import type { SavedParticipantRes, SavedParticipantReq } from 'api-client'
import logger from '~/utils/logger'

/**
 * Composable for managing saved participants.
 * Provides CRUD operations and state management for the user's saved participant list.
 */
export const useSavedParticipants = () => {
  const participants = ref<SavedParticipantRes[]>([])
  const selfParticipant = ref<SavedParticipantRes | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  /**
   * Fetch all saved participants for the current user.
   */
  const fetchParticipants = async () => {
    loading.value = true
    error.value = null
    try {
      const data = await $fetch<SavedParticipantRes[]>('/api/profile/participants', {
        credentials: 'include'
      })
      participants.value = data || []
      // Also extract the self participant if present
      selfParticipant.value = data?.find(p => p.isSelf) || null
    } catch (e) {
      error.value = 'Error al cargar participantes guardados'
      logger.error('Failed to fetch saved participants:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch only the "self" participant (the one marked as the current user).
   */
  const fetchSelfParticipant = async () => {
    try {
      const data = await $fetch<SavedParticipantRes>('/api/profile/participants/self', {
        credentials: 'include'
      })
      selfParticipant.value = data || null
    } catch (e) {
      // 404 is expected if no self participant exists
      const status = (e as { statusCode?: number })?.statusCode
      if (status !== 404) {
        logger.error('Failed to fetch self participant:', e)
      }
      selfParticipant.value = null
    }
  }

  /**
   * Create a new saved participant.
   */
  const createParticipant = async (data: SavedParticipantReq): Promise<SavedParticipantRes | null> => {
    loading.value = true
    error.value = null
    try {
      const result = await $fetch<SavedParticipantRes>('/api/profile/participants', {
        method: 'POST',
        body: data,
        credentials: 'include'
      })
      if (result) {
        participants.value = [result, ...participants.value]
      }
      return result
    } catch (e) {
      error.value = 'Error al crear participante'
      logger.error('Failed to create participant:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Create or update the "self" participant (syncs to user profile).
   */
  const createOrUpdateSelf = async (data: SavedParticipantReq): Promise<SavedParticipantRes | null> => {
    loading.value = true
    error.value = null
    try {
      const result = await $fetch<SavedParticipantRes>('/api/profile/participants/self', {
        method: 'POST',
        body: data,
        credentials: 'include'
      })
      if (result) {
        selfParticipant.value = result
        // Update in the list too
        const idx = participants.value.findIndex(p => p.isSelf)
        if (idx >= 0) {
          participants.value[idx] = result
        } else {
          participants.value = [result, ...participants.value]
        }
      }
      return result
    } catch (e) {
      error.value = 'Error al guardar participante'
      logger.error('Failed to create/update self participant:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Update an existing saved participant.
   */
  const updateParticipant = async (id: string, data: SavedParticipantReq): Promise<SavedParticipantRes | null> => {
    loading.value = true
    error.value = null
    try {
      const result = await $fetch<SavedParticipantRes>(`/api/profile/participants/${id}`, {
        method: 'PUT',
        body: data,
        credentials: 'include'
      })
      if (result) {
        const idx = participants.value.findIndex(p => p.id === id)
        if (idx >= 0) {
          participants.value[idx] = result
        }
        if (result.isSelf) {
          selfParticipant.value = result
        }
      }
      return result
    } catch (e) {
      error.value = 'Error al actualizar participante'
      logger.error('Failed to update participant:', e)
      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Delete a saved participant.
   */
  const deleteParticipant = async (id: string): Promise<boolean> => {
    loading.value = true
    error.value = null
    try {
      await $fetch(`/api/profile/participants/${id}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      participants.value = participants.value.filter(p => p.id !== id)
      if (selfParticipant.value?.id === id) {
        selfParticipant.value = null
      }
      return true
    } catch (e) {
      error.value = 'Error al eliminar participante'
      logger.error('Failed to delete participant:', e)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * Get participant options for a dropdown, with "self" first.
   */
  const participantOptions = computed(() => {
    const sorted = [...participants.value].sort((a, b) => {
      // Self participant first
      if (a.isSelf && !b.isSelf) return -1
      if (!a.isSelf && b.isSelf) return 1
      // Then by name
      return a.fullName.localeCompare(b.fullName)
    })

    return sorted.map(p => ({
      label: p.isSelf ? `${p.fullName} (Yo)` : p.fullName,
      value: p.id,
      participant: p
    }))
  })

  /**
   * Check if user already has a "self" participant.
   */
  const hasSelfParticipant = computed(() => !!selfParticipant.value)

  return {
    participants,
    selfParticipant,
    loading,
    error,
    fetchParticipants,
    fetchSelfParticipant,
    createParticipant,
    createOrUpdateSelf,
    updateParticipant,
    deleteParticipant,
    participantOptions,
    hasSelfParticipant
  }
}
