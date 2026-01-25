import type { TourRes, TourScheduleRes } from 'api-client'
import type { TreeItem } from '@nuxt/ui'
import { getLocalDateString } from '~/utils/dateUtils'
import logger from '~/utils/logger'

export interface MediaHierarchyNode extends TreeItem {
  id: string
  label: string
  icon?: string
  type: 'root' | 'tour' | 'schedule'
  tourId?: string
  scheduleId?: string
  children?: MediaHierarchyNode[]
  defaultExpanded?: boolean
  photoCount?: number
}

export function useMediaHierarchy() {
  const { fetchAdminSchedules } = useAdminData()

  const loading = ref(false)
  const tours = ref<TourRes[]>([])
  const schedulesByTour = ref<Record<string, TourScheduleRes[]>>({})

  /**
   * Load tours (paginated)
   */
  async function loadTours() {
    const adminStore = useAdminStore()
    loading.value = true
    try {
      // Fetch all tours (no pagination for hierarchy - adjust if you have many tours)
      tours.value = await adminStore.fetchTours()
    } catch (error) {
      logger.error('Error loading tours:', error)
      tours.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * Load schedules for a specific tour (past schedules)
   */
  async function loadSchedulesForTour(tourId: string) {
    if (schedulesByTour.value[tourId]) {
      // Already loaded
      return schedulesByTour.value[tourId]
    }

    loading.value = true
    try {
      const ninetyDaysAgo = new Date()
      ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90)
      const startDate = getLocalDateString(ninetyDaysAgo)
      const endDate = getLocalDateString(new Date())
      const schedules = await fetchAdminSchedules({
        tourId,
        mode: 'past', // Only past schedules
        start: startDate,
        end: endDate
      })
      schedulesByTour.value[tourId] = schedules
      return schedules
    } catch (error) {
      logger.error(`Error loading schedules for tour ${tourId}:`, error)
      return []
    } finally {
      loading.value = false
    }
  }

  /**
   * Build tree structure for UTree component
   */
  function buildTree(): MediaHierarchyNode[] {
    return [
      {
        id: 'media-root',
        label: 'Biblioteca de Medios',
        icon: 'i-lucide-folder-open',
        type: 'root',
        defaultExpanded: true,
        children: tours.value.map(tour => ({
          id: tour.id!,
          label: tour.nameTranslations?.es || 'Sin nombre',
          icon: 'i-lucide-image',
          type: 'tour' as const,
          tourId: tour.id!,
          photoCount: tour.images?.length || 0,
          children: [] // Schedules will be loaded on demand
        }))
      }
    ]
  }

  /**
   * Build schedule nodes for a tour
   */
  async function buildScheduleNodes(tourId: string): Promise<MediaHierarchyNode[]> {
    const schedules = await loadSchedulesForTour(tourId)

    return schedules
      .filter(schedule => schedule.id && schedule.startDatetime)
      .map(schedule => ({
        id: schedule.id!,
        // startDatetime is an Instant (ISO with Z), so new Date() works correctly
        label: `${new Date(schedule.startDatetime!).toLocaleDateString('es-CL', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          timeZone: 'America/Santiago'
        })}`,
        icon: 'i-lucide-calendar',
        type: 'schedule' as const,
        scheduleId: schedule.id!,
        tourId
      }))
  }

  return {
    loading,
    tours,
    schedulesByTour,
    loadTours,
    loadSchedulesForTour,
    buildTree,
    buildScheduleNodes
  }
}
