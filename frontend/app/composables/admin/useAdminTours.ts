import type { TourCreateReq, TourUpdateReq, TourRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminTours = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    fetchAdminTours: () =>
      $fetch<TourRes[]>('/api/admin/tours', { ...baseFetchOptions }),

    createAdminTour: (tourData: TourCreateReq) =>
      $fetch<TourRes>('/api/admin/tours', {
        method: 'POST' as const,
        body: tourData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    updateAdminTour: (id: string, tourData: TourUpdateReq) =>
      $fetch<TourRes>(`/api/admin/tours/${id}`, {
        method: 'PUT',
        body: tourData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    deleteAdminTour: (id: string) =>
      $fetch<unknown>(`/api/admin/tours/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      })
  }
}
