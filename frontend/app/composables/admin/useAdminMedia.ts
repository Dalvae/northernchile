import type { MediaRes, MediaUpdateReq, PageMediaRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminMedia = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    // Core media CRUD
    fetchAdminMedia: (params?: Record<string, string | number | boolean | undefined>) =>
      $fetch<PageMediaRes>('/api/admin/media', { params, ...baseFetchOptions }),

    fetchAdminMediaById: (id: string) =>
      $fetch<MediaRes>(`/api/admin/media/${id}`, { ...baseFetchOptions }),

    uploadAdminMedia: (formData: FormData, onProgress?: (progress: number) => void) =>
      $fetch<MediaRes>('/api/admin/media', {
        method: 'POST',
        body: formData,
        ...baseFetchOptions,
        ...(onProgress && {
          onUploadProgress: (event: { total?: number, loaded: number }) => {
            if (event.total) {
              onProgress(Math.round((event.loaded / event.total) * 100))
            }
          }
        })
      }),

    updateAdminMedia: (id: string, mediaData: MediaUpdateReq) =>
      $fetch<MediaRes>(`/api/admin/media/${id}`, {
        method: 'PATCH',
        body: mediaData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    deleteAdminMedia: (id: string) =>
      $fetch<unknown>(`/api/admin/media/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),

    // Gallery management
    fetchTourGallery: (tourId: string) =>
      $fetch<MediaRes[]>(`/api/admin/media/tour/${tourId}/gallery`, { ...baseFetchOptions }),

    fetchScheduleGallery: (scheduleId: string) =>
      $fetch<MediaRes[]>(`/api/admin/media/schedule/${scheduleId}/gallery`, { ...baseFetchOptions }),

    setTourHeroImage: (tourId: string, mediaId: string) =>
      $fetch<unknown>(`/api/admin/media/tour/${tourId}/hero/${mediaId}`, {
        method: 'PUT',
        ...baseFetchOptions
      }),

    toggleTourFeaturedImage: (tourId: string, mediaId: string) =>
      $fetch<unknown>(`/api/admin/media/tour/${tourId}/featured/${mediaId}`, {
        method: 'PUT',
        ...baseFetchOptions
      }),

    assignMediaToTour: (tourId: string, mediaIds: string[]) =>
      $fetch<unknown>(`/api/admin/media/tour/${tourId}/assign`, {
        method: 'POST',
        body: { targetId: tourId, mediaIds },
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    assignMediaToSchedule: (scheduleId: string, mediaIds: string[]) =>
      $fetch<unknown>(`/api/admin/media/schedule/${scheduleId}/assign`, {
        method: 'POST',
        body: { targetId: scheduleId, mediaIds },
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    reorderTourGallery: (tourId: string, orders: Array<{ mediaId: string, displayOrder: number }>) =>
      $fetch<unknown>(`/api/admin/media/tour/${tourId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    reorderScheduleGallery: (scheduleId: string, orders: Array<{ mediaId: string, displayOrder: number }>) =>
      $fetch<unknown>(`/api/admin/media/schedule/${scheduleId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    unassignMediaFromTour: (tourId: string, mediaId: string) =>
      $fetch<unknown>(`/api/admin/media/tour/${tourId}/media/${mediaId}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),

    unassignMediaFromSchedule: (scheduleId: string, mediaId: string) =>
      $fetch<unknown>(`/api/admin/media/schedule/${scheduleId}/media/${mediaId}`, {
        method: 'DELETE',
        ...baseFetchOptions
      })
  }
}
