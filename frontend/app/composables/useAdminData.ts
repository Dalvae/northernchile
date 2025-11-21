// frontend/app/composables/useAdminData.ts
import { computed } from 'vue'
import { useAuthStore } from '~/stores/auth'
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
  AdminPasswordChangeReq,
  MediaRes,
  MediaUpdateReq,
  PageMediaRes
} from 'api-client'

export const useAdminData = () => {
  const authStore = useAuthStore()
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  const headers = computed(() => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }

    if (authStore.token) {
      headers.Authorization = `Bearer ${authStore.token}`
    }

    return headers
  })

  return {
    fetchAdminTours: () =>
      $fetch<TourRes[]>(`${apiBase}/api/admin/tours`, { headers: headers.value }),
    createAdminTour: (tourData: TourCreateReq) =>
      $fetch<TourRes>(`${apiBase}/api/admin/tours`, {
        method: 'POST' as const,
        body: tourData,
        headers: headers.value
      }),
    updateAdminTour: (id: string, tourData: TourUpdateReq) =>
      $fetch<TourRes>(`${apiBase}/api/admin/tours/${id}`, {
        method: 'PUT',
        body: tourData,
        headers: headers.value
      }),
    deleteAdminTour: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/tours/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    fetchAdminSchedules: (params?: Record<string, string>) =>
      $fetch<TourScheduleRes[]>(`${apiBase}/api/admin/schedules`, { params, headers: headers.value }),
    createAdminSchedule: (scheduleData: TourScheduleCreateReq) =>
      $fetch<TourScheduleRes>(`${apiBase}/api/admin/schedules`, {
        method: 'POST' as const,
        body: scheduleData,
        headers: headers.value
      }),
    updateAdminSchedule: (id: string, scheduleData: Partial<TourScheduleCreateReq>) =>
      $fetch<TourScheduleRes>(`${apiBase}/api/admin/schedules/${id}`, {
        method: 'PATCH',
        body: scheduleData,
        headers: headers.value
      }),
    deleteAdminSchedule: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/schedules/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
     fetchAdminBookings: (params?: Record<string, string>) =>
       $fetch<BookingRes[]>(`${apiBase}/api/admin/bookings`, { params, headers: headers.value }),
    fetchAdminBookingById: (id: string) =>
      $fetch<BookingRes>(`${apiBase}/api/admin/bookings/${id}`, { headers: headers.value }),
    updateAdminBooking: (id: string, bookingData: BookingUpdateReq) =>
      $fetch<BookingRes>(`${apiBase}/api/admin/bookings/${id}`, {
        method: 'PUT',
        body: bookingData,
        headers: headers.value
      }),
    deleteAdminBooking: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/bookings/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    fetchAdminUsers: (params?: Record<string, string>) =>
      $fetch<UserRes[]>(`${apiBase}/api/admin/users`, { params, headers: headers.value }),
    fetchAdminUserById: (id: string) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users/${id}`, { headers: headers.value }),
    createAdminUser: (userData: UserCreateReq) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users`, {
        method: 'POST',
        body: userData,
        headers: headers.value
      }),
    updateAdminUser: (id: string, userData: UserUpdateReq) =>
      $fetch<UserRes>(`${apiBase}/api/admin/users/${id}`, {
        method: 'PUT',
        body: userData,
        headers: headers.value
      }),
    deleteAdminUser: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/users/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    resetAdminUserPassword: (id: string, newPassword: string) =>
      $fetch<void>(`${apiBase}/api/admin/users/${id}/password`, {
        method: 'PUT',
        body: { newPassword },
        headers: headers.value
      }),
     fetchAdminAlertsCount: () =>
       $fetch<{ pending: number }>(`${apiBase}/api/admin/alerts/count`, {
         headers: headers.value
       }),
     fetchAdminSettings: () =>
       $fetch<Record<string, unknown>>(`${apiBase}/api/admin/settings`, { headers: headers.value }),
     updateAdminSettings: (settings: Record<string, unknown>) =>
       $fetch<Record<string, unknown>>(`${apiBase}/api/admin/settings`, {
         method: 'PATCH',
         body: settings,
         headers: headers.value
       }),
    // Media management
    fetchAdminMedia: (params?: Record<string, any>) =>
      $fetch<PageMediaRes>(`${apiBase}/api/admin/media`, { params, headers: headers.value }),
    fetchAdminMediaById: (id: string) =>
      $fetch<MediaRes>(`${apiBase}/api/admin/media/${id}`, { headers: headers.value }),
    uploadAdminMedia: (formData: FormData, onProgress?: (progress: number) => void) =>
      $fetch<MediaRes>(`${apiBase}/api/admin/media`, {
        method: 'POST',
        body: formData,
        headers: {
          Authorization: headers.value.Authorization || ''
          // Don't set Content-Type, let browser set it with boundary for multipart
        },
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
        headers: headers.value
      }),
    deleteAdminMedia: (id: string) =>
      $fetch<void>(`${apiBase}/api/admin/media/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    // Media gallery management
    fetchTourGallery: (tourId: string) =>
      $fetch<MediaRes[]>(`${apiBase}/api/admin/media/tour/${tourId}/gallery`, { headers: headers.value }),
    fetchScheduleGallery: (scheduleId: string) =>
      $fetch<MediaRes[]>(`${apiBase}/api/admin/media/schedule/${scheduleId}/gallery`, { headers: headers.value }),
    setTourHeroImage: (tourId: string, mediaId: string) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/hero/${mediaId}`, {
        method: 'PUT',
        headers: headers.value
      }),
    assignMediaToTour: (tourId: string, mediaIds: string[]) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/assign`, {
        method: 'POST',
        body: { targetId: tourId, mediaIds },
        headers: headers.value
      }),
    assignMediaToSchedule: (scheduleId: string, mediaIds: string[]) =>
      $fetch<void>(`${apiBase}/api/admin/media/schedule/${scheduleId}/assign`, {
        method: 'POST',
        body: { targetId: scheduleId, mediaIds },
        headers: headers.value
      }),
    reorderTourGallery: (tourId: string, orders: Array<{ mediaId: string; displayOrder: number }>) =>
      $fetch<void>(`${apiBase}/api/admin/media/tour/${tourId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: headers.value
      }),
    reorderScheduleGallery: (scheduleId: string, orders: Array<{ mediaId: string; displayOrder: number }>) =>
      $fetch<void>(`${apiBase}/api/admin/media/schedule/${scheduleId}/reorder`, {
        method: 'PUT',
        body: orders,
        headers: headers.value
      })
  }
}
