import type { TourRes } from 'api-client'

export const useAdminToursData = () => {
  const adminStore = useAdminStore()

  return useAsyncData<TourRes[]>('admin-tours-data', () => adminStore.fetchTours(), {
    server: false,
    lazy: true
  })
}
