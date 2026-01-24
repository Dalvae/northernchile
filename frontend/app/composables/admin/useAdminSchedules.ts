import type { TourScheduleCreateReq, TourScheduleRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminSchedules = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    fetchAdminSchedules: (params?: Record<string, string>) =>
      $fetch<TourScheduleRes[]>('/api/admin/schedules', { params, ...baseFetchOptions }),

    createAdminSchedule: (scheduleData: TourScheduleCreateReq) =>
      $fetch<TourScheduleRes>('/api/admin/schedules', {
        method: 'POST' as const,
        body: scheduleData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    updateAdminSchedule: (id: string, scheduleData: Partial<TourScheduleCreateReq>) =>
      $fetch<TourScheduleRes>(`/api/admin/schedules/${id}`, {
        method: 'PATCH',
        body: scheduleData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    deleteAdminSchedule: (id: string) =>
      $fetch<unknown>(`/api/admin/schedules/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      })
  }
}
