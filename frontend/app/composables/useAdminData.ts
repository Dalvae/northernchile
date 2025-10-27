import { useAuthStore } from '~/stores/auth';
import { computed } from 'vue';

export const useAdminData = () => {
  const authStore = useAuthStore();
  const headers = computed(() => {
    const headers: { 'Content-Type': string; Authorization?: string } = {
      'Content-Type': 'application/json',
    };
    if (authStore.token) {
      headers.Authorization = `Bearer ${authStore.token}`;
    }
    return headers;
  });

  return {
    // CAMBIO AQUÍ: La ruta debe ser '/api/tours' en lugar de '/api/admin/tours'
    fetchAdminTours: async () => { const { data } = await useFetch('/api/tours', { headers }); return data.value; },
    
    createAdminTour: async (tourData: any) => { const { data } = await useFetch('/api/admin/tours', { method: 'POST', body: tourData, headers }); return data.value; },
    updateAdminTour: async (id: string, tourData: any) => { const { data } = await useFetch(`/api/admin/tours/${id}`, { method: 'PUT', body: tourData, headers }); return data.value; },
    deleteAdminTour: async (id: string) => { const { data } = await useFetch(`/api/admin/tours/${id}`, { method: 'DELETE', headers }); return data.value; },
    
    // ... el resto de las funciones permanecen igual
    fetchAdminSchedules: async (params?: any) => { const { data } = await useFetch('/api/admin/schedules', { params, headers }); return data.value; },
    createAdminSchedule: async (scheduleData: any) => { const { data } = await useFetch('/api/admin/schedules', { method: 'POST', body: scheduleData, headers }); return data.value; },
    updateAdminSchedule: async (id: string, scheduleData: any) => { const { data } = await useFetch(`/api/admin/schedules/${id}`, { method: 'PUT', body: scheduleData, headers }); return data.value; },
    deleteAdminSchedule: async (id: string) => { const { data } = await useFetch('/api/admin/schedules', { method: 'DELETE', headers }); return data.value; },
    fetchAdminBookings: async (params?: any) => { const { data } = await useFetch<any>('/api/admin/bookings', { params, headers }); return data.value; },
    fetchAdminBookingById: async (id: string) => { const { data } = await useFetch(`/api/admin/bookings/${id}`, { headers }); return data.value; },
    updateAdminBooking: async (id: string, bookingData: any) => { const { data } = await useFetch(`/api/admin/bookings/${id}`, { method: 'PUT', body: bookingData, headers }); return data.value; },
    deleteAdminBooking: async (id: string) => { const { data } = await useFetch('/api/admin/bookings', { method: 'DELETE', headers }); return data.value; },
    fetchAdminUsers: async (params?: any) => { const { data } = await useFetch('/api/admin/users', { params, headers }); return data.value; },
    fetchAdminUserById: async (id: string) => { const { data } = await useFetch(`/api/admin/users/${id}`, { headers }); return data.value; },
    updateAdminUser: async (id: string, userData: any) => { const { data } = await useFetch(`/api/admin/users/${id}`, { method: 'PUT', body: userData, headers }); return data.value; },
    deleteAdminUser: async (id: string) => { const { data } = await useFetch('/api/admin/users', { method: 'DELETE', headers }); return data.value; },
  };
};