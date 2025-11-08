// frontend/app/composables/useAdminData.ts
import { useAuthStore } from '~/stores/auth'
import { computed } from 'vue'

export const useAdminData = () => {
  const authStore = useAuthStore()

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
      $fetch('/api/admin/tours', { headers: headers.value }),
    createAdminTour: (tourData: any) =>
      $fetch('/api/admin/tours', {
        method: 'POST',
        body: tourData,
        headers: headers.value
      }),
    updateAdminTour: (id: string, tourData: any) =>
      $fetch(`/api/admin/tours/${id}`, {
        method: 'PUT',
        body: tourData,
        headers: headers.value
      }),
    deleteAdminTour: (id: string) =>
      $fetch(`/api/admin/tours/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    // ... (asegúrate de que todas las funciones usen headers: headers.value)
    fetchAdminSchedules: (params?: any) =>
      $fetch('/api/admin/schedules', { params, headers: headers.value }),
    createAdminSchedule: (scheduleData: any) =>
      $fetch('/api/admin/schedules', {
        method: 'POST',
        body: scheduleData,
        headers: headers.value
      }),
    updateAdminSchedule: (id: string, scheduleData: any) =>
      $fetch(`/api/admin/schedules/${id}`, {
        method: 'PUT',
        body: scheduleData,
        headers: headers.value
      }),
    deleteAdminSchedule: (id: string) =>
      $fetch(`/api/admin/schedules/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    fetchAdminBookings: (params?: any) =>
      $fetch<any>('/api/admin/bookings', { params, headers: headers.value }),
    fetchAdminBookingById: (id: string) =>
      $fetch(`/api/admin/bookings/${id}`, { headers: headers.value }),
    updateAdminBooking: (id: string, bookingData: any) =>
      $fetch(`/api/admin/bookings/${id}`, {
        method: 'PUT',
        body: bookingData,
        headers: headers.value
      }),
    deleteAdminBooking: (id: string) =>
      $fetch(`/api/admin/bookings/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    fetchAdminUsers: (params?: any) =>
      $fetch('/api/admin/users', { params, headers: headers.value }),
    fetchAdminUserById: (id: string) =>
      $fetch(`/api/admin/users/${id}`, { headers: headers.value }),
    createAdminUser: (userData: any) =>
      $fetch('/api/admin/users', {
        method: 'POST',
        body: userData,
        headers: headers.value
      }),
    updateAdminUser: (id: string, userData: any) =>
      $fetch(`/api/admin/users/${id}`, {
        method: 'PUT',
        body: userData,
        headers: headers.value
      }),
    deleteAdminUser: (id: string) =>
      $fetch(`/api/admin/users/${id}`, {
        method: 'DELETE',
        headers: headers.value
      }),
    resetAdminUserPassword: (id: string, newPassword: string) =>
      $fetch(`/api/admin/users/${id}/password`, {
        method: 'PUT',
        body: { newPassword },
        headers: headers.value
      }),
     fetchAdminAlertsCount: () =>
       $fetch<{ pending: number }>('/api/admin/alerts/count', {
         headers: headers.value
       }),
     fetchAdminSettings: () =>
       $fetch('/api/admin/settings', { headers: headers.value }),
     updateAdminSettings: (settings: any) =>
       $fetch('/api/admin/settings', {
         method: 'PUT',
         body: settings,
         headers: headers.value
       })
  }
}
