import { useAuthStore } from '~/stores/auth';
import { computed } from 'vue';

export const useAdminData = () => {
  const authStore = useAuthStore();

  // Usamos un `computed` para que los headers sean siempre reactivos al token.
  const headers = computed(() => {
    const headers: { 'Content-Type': string; Authorization?: string } = {
      'Content-Type': 'application/json',
    };
    if (authStore.token) {
      headers.Authorization = `Bearer ${authStore.token}`;
    }
    return headers;
  });

  // Pasamos el `computed` directamente a `useFetch`, que sabe cómo manejarlo.
  const fetchAdminTours = () => {
    const { data, pending, error } = useFetch('/api/admin/tours', { headers: headers.value });
    return { data, pending, error };
  };
  const createAdminTour = (tourData: any) => {
    const { data, pending, error } = useFetch('/api/admin/tours', { method: 'POST', body: tourData, headers: headers.value });
    return { data, pending, error };
  };
  const updateAdminTour = (id: string, tourData: any) => {
    const { data, pending, error } = useFetch(`/api/admin/tours/${id}`, { method: 'PUT', body: tourData, headers: headers.value });
    return { data, pending, error };
  };
  const deleteAdminTour = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/tours/${id}`, { method: 'DELETE', headers: headers.value });
    return { data, pending, error };
  };

  const fetchAdminSchedules = (params?: any) => {
    const { data, pending, error } = useFetch('/api/admin/schedules', { params, headers: headers.value });
    return { data, pending, error };
  };
  const createAdminSchedule = (scheduleData: any) => {
    const { data, pending, error } = useFetch('/api/admin/schedules', { method: 'POST', body: scheduleData, headers: headers.value });
    return { data, pending, error };
  };
  const updateAdminSchedule = (id: string, scheduleData: any) => {
    const { data, pending, error } = useFetch(`/api/admin/schedules/${id}`, { method: 'PUT', body: scheduleData, headers: headers.value });
    return { data, pending, error };
  };
  const deleteAdminSchedule = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/schedules/${id}`, { method: 'DELETE', headers: headers.value });
    return { data, pending, error };
  };

  const fetchAdminBookings = (params?: any) => {
    const { data, pending, error } = useFetch('/api/admin/bookings', { params, headers: headers.value });
    return { data, pending, error };
  };
  const fetchAdminBookingById = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/bookings/${id}`, { headers: headers.value });
    return { data, pending, error };
  };
  const updateAdminBooking = (id: string, bookingData: any) => {
    const { data, pending, error } = useFetch(`/api/admin/bookings/${id}`, { method: 'PUT', body: bookingData, headers: headers.value });
    return { data, pending, error };
  };
  const deleteAdminBooking = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/bookings/${id}`, { method: 'DELETE', headers: headers.value });
    return { data, pending, error };
  };

  const fetchAdminUsers = (params?: any) => {
    const { data, pending, error } = useFetch('/api/admin/users', { params, headers: headers.value });
    return { data, pending, error };
  };
  const fetchAdminUserById = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/users/${id}`, { headers: headers.value });
    return { data, pending, error };
  };
  const updateAdminUser = (id: string, userData: any) => {
    const { data, pending, error } = useFetch(`/api/admin/users/${id}`, { method: 'PUT', body: userData, headers: headers.value });
    return { data, pending, error };
  };
  const deleteAdminUser = (id: string) => {
    const { data, pending, error } = useFetch(`/api/admin/users/${id}`, { method: 'DELETE', headers: headers.value });
    return { data, pending, error };
  };

  return {
    fetchAdminTours,
    createAdminTour,
    updateAdminTour,
    deleteAdminTour,
    fetchAdminSchedules,
    createAdminSchedule,
    updateAdminSchedule,
    deleteAdminSchedule,
    fetchAdminBookings,
    fetchAdminBookingById,
    updateAdminBooking,
    deleteAdminBooking,
    fetchAdminUsers,
    fetchAdminUserById,
    updateAdminUser,
    deleteAdminUser,
  };
};