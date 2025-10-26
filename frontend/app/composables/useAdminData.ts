import { useAuthStore } from "~/stores/auth";
import { computed } from "vue";

export const useAdminData = () => {
  const authStore = useAuthStore();

  // Usamos un `computed` para que los headers sean siempre reactivos al token.
  const headers = computed(() => {
    const headers: { "Content-Type": string; Authorization?: string } = {
      "Content-Type": "application/json",
    };
    if (authStore.token) {
      headers.Authorization = `Bearer ${authStore.token}`;
    }
    return headers;
  });

  // Pasamos el `computed` directamente a `useFetch`, que sabe cómo manejarlo.
  const fetchAdminTours = () => useFetch("/api/admin/tours", { headers });
  const createAdminTour = (tourData: any) =>
    useFetch("/api/admin/tours", { method: "POST", body: tourData, headers });
  const updateAdminTour = (id: string, tourData: any) =>
    useFetch(`/api/admin/tours/${id}`, {
      method: "PUT",
      body: tourData,
      headers,
    });
  const deleteAdminTour = (id: string) =>
    useFetch(`/api/admin/tours/${id}`, { method: "DELETE", headers });

  const fetchAdminSchedules = (params?: any) =>
    useFetch("/api/admin/schedules", { params, headers });
  const createAdminSchedule = (scheduleData: any) =>
    useFetch("/api/admin/schedules", {
      method: "POST",
      body: scheduleData,
      headers,
    });
  const updateAdminSchedule = (id: string, scheduleData: any) =>
    useFetch(`/api/admin/schedules/${id}`, {
      method: "PUT",
      body: scheduleData,
      headers,
    });
  const deleteAdminSchedule = (id: string) =>
    useFetch(`/api/admin/schedules/${id}`, { method: "DELETE", headers });

  const fetchAdminBookings = (params?: any) =>
    useFetch("/api/admin/bookings", { params, headers });
  const fetchAdminBookingById = (id: string) =>
    useFetch(`/api/admin/bookings/${id}`, { headers });
  const updateAdminBooking = (id: string, bookingData: any) =>
    useFetch(`/api/admin/bookings/${id}`, {
      method: "PUT",
      body: bookingData,
      headers,
    });
  const deleteAdminBooking = (id: string) =>
    useFetch(`/api/admin/bookings/${id}`, { method: "DELETE", headers });

  const fetchAdminUsers = (params?: any) =>
    useFetch("/api/admin/users", { params, headers });
  const fetchAdminUserById = (id: string) =>
    useFetch(`/api/admin/users/${id}`, { headers });
  const updateAdminUser = (id: string, userData: any) =>
    useFetch(`/api/admin/users/${id}`, {
      method: "PUT",
      body: userData,
      headers,
    });
  const deleteAdminUser = (id: string) =>
    useFetch(`/api/admin/users/${id}`, { method: "DELETE", headers });

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

