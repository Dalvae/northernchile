import { useAuthStore } from '~/stores/auth';
import { computed } from 'vue';

export const useAdminData = () => {
  const authStore = useAuthStore();

  const headers = computed(() => {
    const headers: { 'Content-Type': string; Authorization?: string } = {
      'Content-Type': 'application/json',
    };
    if (authStore.token) {
      // Importante: El backend espera el token sin el prefijo "Bearer" en este caso.
      // Asegúrate de que tu filtro JWT en Spring Boot lo maneje así.
      // Si tu backend espera "Bearer [token]", entonces sería `Bearer ${authStore.token}`.
      headers.Authorization = `Bearer ${authStore.token}`;
    }
    return headers;
  });

  // Devolvemos directamente el resultado de useFetch, que ya es reactivo.
  return {
    fetchAdminTours: () => useFetch('/api/admin/tours', { headers }),
    createAdminTour: (tourData: any) => useFetch('/api/admin/tours', { method: 'POST', body: tourData, headers }),
    updateAdminTour: (id: string, tourData: any) => useFetch(`/api/admin/tours/${id}`, { method: 'PUT', body: tourData, headers }),
    deleteAdminTour: (id: string) => useFetch(`/api/admin/tours/${id}`, { method: 'DELETE', headers }),

    fetchAdminSchedules: (params?: any) => useFetch('/api/admin/schedules', { params, headers }),
    createAdminSchedule: (scheduleData: any) => useFetch('/api/admin/schedules', { method: 'POST', body: scheduleData, headers }),
    updateAdminSchedule: (id: string, scheduleData: any) => useFetch(`/api/admin/schedules/${id}`, { method: 'PUT', body: scheduleData, headers }),
    deleteAdminSchedule: (id: string) => useFetch('/api/admin/schedules', { method: 'DELETE', headers }),

    fetchAdminBookings: (params?: any) => useFetch<any>('/api/admin/bookings', { params, headers }),
    fetchAdminBookingById: (id: string) => useFetch(`/api/admin/bookings/${id}`, { headers }),
    updateAdminBooking: (id: string, bookingData: any) => useFetch(`/api/admin/bookings/${id}`, { method: 'PUT', body: bookingData, headers }),
    deleteAdminBooking: (id: string) => useFetch('/api/admin/bookings', { method: 'DELETE', headers }),

    fetchAdminUsers: (params?: any) => useFetch('/api/admin/users', { params, headers }),
    fetchAdminUserById: (id: string) => useFetch(`/api/admin/users/${id}`, { headers }),
    updateAdminUser: (id: string, userData: any) => useFetch(`/api/admin/users/${id}`, { method: 'PUT', body: userData, headers }),
    deleteAdminUser: (id: string) => useFetch('/api/admin/users', { method: 'DELETE', headers }),
  };
};