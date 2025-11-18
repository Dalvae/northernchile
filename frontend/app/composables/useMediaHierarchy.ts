import type { TourRes, TourScheduleRes } from 'api-client'

export interface MediaHierarchyNode {
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
  const { fetchAdminTours, fetchAdminSchedules } = useAdminData()

  const loading = ref(false)
  const tours = ref<TourRes[]>([])
  const schedulesByTour = ref<Record<string, TourScheduleRes[]>>({})

  /**
   * Load tours (paginated)
   */
  async function loadTours() {
    loading.value = true
    try {
      // Fetch all tours (no pagination for hierarchy - adjust if you have many tours)
      tours.value = await fetchAdminTours()
    } catch (error) {
      console.error('Error loading tours:', error)
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
      const schedules = await fetchAdminSchedules({
        tourId,
        mode: 'past', // Only past schedules
        start: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // Last 90 days
        end: new Date().toISOString().split('T')[0]
      })
      schedulesByTour.value[tourId] = schedules
      return schedules
    } catch (error) {
      console.error(`Error loading schedules for tour ${tourId}:`, error)
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

    return schedules.map(schedule => ({
      id: schedule.id!,
      label: `${new Date(schedule.startDatetime).toLocaleDateString('es-CL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
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
