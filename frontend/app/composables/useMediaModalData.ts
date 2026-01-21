import type { TourRes, TourScheduleRes } from 'api-client'

export interface MediaModalData {
  tours: Ref<TourRes[]>
  schedules: Ref<TourScheduleRes[]>
  tourOptions: ComputedRef<Array<{ label: string; value: string | undefined }>>
  scheduleOptions: ComputedRef<Array<{ label: string; value: string | undefined }>>
  loading: Ref<boolean>
  loaded: Ref<boolean>
  load: () => Promise<void>
}

export function useMediaModalData(): MediaModalData {
  const adminStore = useAdminStore()
  const { fetchAdminSchedules } = useAdminData()

  const tours = ref<TourRes[]>([])
  const schedules = ref<TourScheduleRes[]>([])
  const loading = ref(false)
  const loaded = ref(false)

  const tourOptions = computed(() => [
    { label: 'Sin asignar', value: undefined },
    ...tours.value
      .filter(t => t?.id)
      .map(t => ({
        label: t.nameTranslations?.es || 'Sin nombre',
        value: t.id
      }))
  ])

  const scheduleOptions = computed(() => [
    { label: 'Sin asignar', value: undefined },
    ...schedules.value
      .filter(s => s?.id)
      .map(s => ({
        label: `${s.tourName} - ${s.startDatetime ? new Date(s.startDatetime).toLocaleDateString('es-CL') : ''}`,
        value: s.id
      }))
  ])

  async function load() {
    if (loaded.value || loading.value) return

    loading.value = true
    try {
      const [toursData, schedulesData] = await Promise.all([
        adminStore.fetchTours(),
        fetchAdminSchedules({ mode: 'past' })
      ])
      tours.value = toursData || []
      schedules.value = schedulesData || []
      loaded.value = true
    } catch (error) {
      console.error('Error loading media modal data:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    tours,
    schedules,
    tourOptions,
    scheduleOptions,
    loading,
    loaded,
    load
  }
}
