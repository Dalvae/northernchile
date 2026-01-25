import type {
  SystemSettingsRes,
  SystemSettingsUpdateReq,
  OverviewReport,
  BookingsByDayReport,
  TopTourReport,
  AuditLogRes,
  PrivateTourRequest
} from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export interface PageAuditLogRes {
  data: AuditLogRes[]
  totalItems: number
  totalPages: number
  currentPage: number
}

export const useAdminReports = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    // Reports
    fetchReportsOverview: (startDate: string, endDate: string) =>
      $fetch<OverviewReport>('/api/admin/reports/overview', {
        params: { startDate, endDate },
        ...baseFetchOptions
      }),

    fetchBookingsByDayReport: (startDate: string, endDate: string) =>
      $fetch<BookingsByDayReport[]>('/api/admin/reports/bookings-by-day', {
        params: { startDate, endDate },
        ...baseFetchOptions
      }),

    fetchTopToursReport: (startDate: string, endDate: string, limit: number = 5) =>
      $fetch<TopTourReport[]>('/api/admin/reports/top-tours', {
        params: { startDate, endDate, limit },
        ...baseFetchOptions
      }),

    // Audit Logs
    fetchAuditLogs: (params?: Record<string, string>) =>
      $fetch<PageAuditLogRes>('/api/admin/audit-logs', { params, ...baseFetchOptions }),

    fetchAuditStats: () =>
      $fetch<{
        totalLogs: number
        createActions: number
        updateActions: number
        deleteActions: number
      }>('/api/admin/audit-logs/stats', { ...baseFetchOptions }),

    // Settings
    fetchAdminSettings: () =>
      $fetch<SystemSettingsRes>('/api/admin/settings', { ...baseFetchOptions }),

    updateAdminSettings: (settings: SystemSettingsUpdateReq) =>
      $fetch<SystemSettingsRes>('/api/admin/settings', {
        method: 'PATCH',
        body: settings,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    // Alerts
    fetchAdminAlertsCount: () =>
      $fetch<{ pending: number }>('/api/admin/alerts/count', {
        ...baseFetchOptions
      }),

    // Private Tour Requests
    fetchPrivateRequests: () =>
      $fetch<PrivateTourRequest[]>('/api/admin/private-tours/requests', { ...baseFetchOptions }),

    updatePrivateRequest: (id: string, data: { status: string, quotedPrice?: number | null }) =>
      $fetch<PrivateTourRequest>(`/api/admin/private-tours/requests/${id}`, {
        method: 'PUT',
        body: data,
        headers: jsonHeaders,
        ...baseFetchOptions
      })
  }
}
