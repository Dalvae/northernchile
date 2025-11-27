// frontend/app/composables/useAdminData.ts
import type {
  TourCreateReq,
  TourUpdateReq,
  TourRes,
  TourScheduleCreateReq,
  TourScheduleRes,
  BookingRes,
  BookingUpdateReq,
  UserCreateReq,
  UserUpdateReq,
  UserRes,
  MediaRes,
  MediaUpdateReq,
  PageMediaRes
} from 'api-client'

export const useAdminData = () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  // Base fetch options with credentials for HttpOnly cookie auth
  const baseFetchOptions = {
    credentials: 'include' as RequestCredentials
  }

  const jsonHeaders = {
    'Content-Type': 'application/json'
  }

  return {
    fetchAdminTours: () =>
      $fetch<TourRes[]>(`${apiBase}/api/admin/tours`, { ...baseFetchOptions }),
    createAdminTour: (tourData: TourCreateReq) =>
      $fetch<TourRes>(`${apiBase}/api/admin/tours`, {
        method: 'POST' as const,
        body: tourData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    updateAdminTour: (id: string, tourData: TourUpdateReq) =>
      $fetch<TourRes>(`${apiBase}/api/admin/tours/${id}`, {
        method: 'PUT',
        body: tourData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    deleteAdminTour: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/tours/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),
    fetchAdminSchedules: (params?: Record<string, string>) =>
      $fetch<TourScheduleRes[]>(`${apiBase}/api/admin/schedules`, { params, ...baseFetchOptions }),
    createAdminSchedule: (scheduleData: TourScheduleCreateReq) =>
      $fetch<TourScheduleRes>(`${apiBase}/api/admin/schedules`, {
        method: 'POST' as const,
        body: scheduleData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    updateAdminSchedule: (id: string, scheduleData: Partial<TourScheduleCreateReq>) =>
      $fetch<TourScheduleRes>(`${apiBase}/api/admin/schedules/${id}`, {
        method: 'PATCH',
        body: scheduleData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    deleteAdminSchedule: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/schedules/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),
    fetchAdminBookings: (params?: Record<string, string>) =>
      $fetch<BookingRes[]>(`${apiBase}/api/admin/bookings`, { params, ...baseFetchOptions }),
    fetchAdminBookingById: (id: string) =>
      $fetch<BookingRes>(`${apiBase}/api/admin/bookings/${id}`, { ...baseFetchOptions }),
    updateAdminBooking: (id: string, bookingData: BookingUpdateReq) =>
      $fetch<BookingRes>(`${apiBase}/api/admin/bookings/${id}`, {
        method: 'PUT',
        body: bookingData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    deleteAdminBooking: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/bookings/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),
    fetchAdminUsers: (params?: Record<string, string>) =>
      $fetch<UserRes[]>(`${apiBase}/api/admin/users`, { params, ...baseFetchOptions }),
    fetchAdminUserById: (id: string) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users/${id}`, { ...baseFetchOptions }),
    createAdminUser: (userData: UserCreateReq) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users`, {
        method: 'POST',
        body: userData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    updateAdminUser: (id: string, userData: UserUpdateReq) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users/${id}`, {
        method: 'PUT',
        body: userData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    deleteAdminUser: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/users/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),
    resetAdminUserPassword: (id: string, newPassword: string) =>
      $fetch<void>(`${apiBase}/api/admin/users/${id}/password`, {
        method: 'PUT',
        body: { newPassword },
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    fetchAdminAlertsCount: () =>
      $fetch<{ pending: number }>(`${apiBase}/api/admin/alerts/count`, {
        ...baseFetchOptions
      }),
    fetchAdminSettings: () =>
      $fetch<Record<string, unknown>>(`${apiBase}/api/admin/settings`, { ...baseFetchOptions }),
    updateAdminSettings: (settings: Record<string, unknown>) =>
      $fetch<Record<string, unknown>>(`${apiBase}/api/admin/settings`, {
        method: 'PATCH',
        body: settings,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    // Media management
    fetchAdminMedia: (params?: Record<string, any>) =>
      $fetch<PageMediaRes>(`${apiBase}/api/admin/media`, { params, ...baseFetchOptions }),
    fetchAdminMediaById: (id: string) =>
      $fetch<MediaRes>(`${apiBase}/api/admin/media/${id}`, { ...baseFetchOptions }),
    uploadAdminMedia: (formData: FormData, onProgress?: (progress: number) => void) =>
      $fetch<MediaRes>(`${apiBase}/api/admin/media`, {
        method: 'POST',
        body: formData,
        // Don't set Content-Type header, let browser set it with boundary for multipart
        ...baseFetchOptions,
        ...(onProgress && {
          onUploadProgress: (event: any) => {
            if (event.total) {
              onProgress(Math.round((event.loaded / event.total) * 100))
            }
          }
        })
      }),
    updateAdminMedia: (id: string, mediaData: MediaUpdateReq) =>
      $fetch<MediaRes>(`${apiBase}/api/admin/media/${id}`, {
        method: 'PATCH',
        body: mediaData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    deleteAdminMedia: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/media/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),
    // Media gallery management
    fetchTourGallery: (tourId: string) =>
      $fetch<MediaRes[]>(`${apiBase}/api/admin/media/tour/${tourId}/gallery`, { ...baseFetchOptions }),
    fetchScheduleGallery: (scheduleId: string) =>
      $fetch<MediaRes[]>(`${apiBase}/api/admin/media/schedule/${scheduleId}/gallery`, { ...baseFetchOptions }),
    setTourHeroImage: (tourId: string, mediaId: string) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/hero/${mediaId}`, {
        method: 'PUT',
        ...baseFetchOptions
      }),
    toggleTourFeaturedImage: (tourId: string, mediaId: string) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/featured/${mediaId}`, {
        method: 'PUT',
        ...baseFetchOptions
      }),
    assignMediaToTour: (tourId: string, mediaIds: string[]) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/assign`, {
        method: 'POST',
        body: { targetId: tourId, mediaIds },
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    assignMediaToSchedule: (scheduleId: string, mediaIds: string[]) =>
      $fetch<void>(`${apiBase}/api/admin/media/schedule/${scheduleId}/assign`, {
        method: 'POST',
        body: { targetId: scheduleId, mediaIds },
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    reorderTourGallery: (tourId: string, orders: Array<{ mediaId: string, displayOrder: number }>) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),
    reorderScheduleGallery: (scheduleId: string, orders: Array<{ mediaId: string, displayOrder: number }>) =>
      $fetch<void>(`${apiBase}/api/admin/media/schedule/${scheduleId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: jsonHeaders,
        ...baseFetchOptions
      })
  }
}
