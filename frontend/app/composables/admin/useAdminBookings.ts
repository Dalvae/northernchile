import type { BookingRes, BookingUpdateReq, PageBookingRes, RefundRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminBookings = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    fetchAdminBookings: (params?: Record<string, string>) =>
      $fetch<PageBookingRes>('/api/admin/bookings', { params, ...baseFetchOptions }),

    fetchAdminBookingById: (id: string) =>
      $fetch<BookingRes>(`/api/admin/bookings/${id}`, { ...baseFetchOptions }),

    updateAdminBooking: (id: string, bookingData: BookingUpdateReq) =>
      $fetch<BookingRes>(`/api/admin/bookings/${id}`, {
        method: 'PUT',
        body: bookingData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    deleteAdminBooking: (id: string) =>
      $fetch<unknown>(`/api/admin/bookings/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),

    refundBooking: (bookingId: string, adminOverride: boolean = false) =>
      $fetch<RefundRes>(`/api/refunds/booking/${bookingId}?adminOverride=${adminOverride}`, {
        method: 'POST',
        ...baseFetchOptions
      })
  }
}
