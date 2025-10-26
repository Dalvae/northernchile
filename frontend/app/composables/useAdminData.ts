// frontend/app/composables/useAdminData.ts
export const useAdminData = () => {
  const authStore = useAuthStore(); // Assuming the auth store is available

  const headers = computed(() => ({
    'Authorization': `Bearer ${authStore.token}`,
    'Content-Type': 'application/json'
  }));

  // Tours
  const fetchAdminTours = () => useFetch('/api/admin/tours', { headers: headers.value });
  const createAdminTour = (tourData: any) => useFetch('/api/admin/tours', { method: 'POST', body: tourData, headers: headers.value });
  const updateAdminTour = (id: string, tourData: any) => useFetch(`/api/admin/tours/${id}`, { method: 'PUT', body: tourData, headers: headers.value });
  const deleteAdminTour = (id: string) => useFetch(`/api/admin/tours/${id}`, { method: 'DELETE', headers: headers.value });

  // Schedules
  const fetchAdminSchedules = (params?: any) => useFetch('/api/admin/schedules', { params, headers: headers.value });
  const createAdminSchedule = (scheduleData: any) => useFetch('/api/admin/schedules', { method: 'POST', body: scheduleData, headers: headers.value });
  const updateAdminSchedule = (id: string, scheduleData: any) => useFetch(`/api/admin/schedules/${id}`, { method: 'PUT', body: scheduleData, headers: headers.value });
  const deleteAdminSchedule = (id: string) => useFetch(`/api/admin/schedules/${id}`, { method: 'DELETE', headers: headers.value });

  // Bookings
  const fetchAdminBookings = (params?: any) => useFetch('/api/admin/bookings', { params, headers: headers.value });
  const fetchAdminBookingById = (id: string) => useFetch(`/api/admin/bookings/${id}`, { headers: headers.value });
  const updateAdminBooking = (id: string, bookingData: any) => useFetch(`/api/admin/bookings/${id}`, { method: 'PUT', body: bookingData, headers: headers.value });
  const deleteAdminBooking = (id: string) => useFetch(`/api/admin/bookings/${id}`, { method: 'DELETE', headers: headers.value });

  // Users
  const fetchAdminUsers = (params?: any) => useFetch('/api/admin/users', { params, headers: headers.value });
  const fetchAdminUserById = (id: string) => useFetch(`/api/admin/users/${id}`, { headers: headers.value });
  const updateAdminUser = (id: string, userData: any) => useFetch(`/api/admin/users/${id}`, { method: 'PUT', body: userData, headers: headers.value });
  const deleteAdminUser = (id: string) => useFetch(`/api/admin/users/${id}`, { method: 'DELETE', headers: headers.value });

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