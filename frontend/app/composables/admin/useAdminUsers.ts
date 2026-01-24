import type { UserCreateReq, UserUpdateReq, UserRes, PageUserRes } from 'api-client'
import { useAdminFetch } from './useAdminFetch'

export const useAdminUsers = () => {
  const { baseFetchOptions, jsonHeaders } = useAdminFetch()

  return {
    fetchAdminUsers: (params?: Record<string, string>) =>
      $fetch<PageUserRes>('/api/admin/users', { params, ...baseFetchOptions }),

    fetchAdminUserById: (id: string) =>
      $fetch<UserRes>(`/api/admin/users/${id}`, { ...baseFetchOptions }),

    createAdminUser: (userData: UserCreateReq) =>
      $fetch<UserRes>('/api/admin/users', {
        method: 'POST',
        body: userData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    updateAdminUser: (id: string, userData: UserUpdateReq) =>
      $fetch<UserRes>(`/api/admin/users/${id}`, {
        method: 'PUT',
        body: userData,
        headers: jsonHeaders,
        ...baseFetchOptions
      }),

    deleteAdminUser: (id: string) =>
      $fetch<unknown>(`/api/admin/users/${id}`, {
        method: 'DELETE',
        ...baseFetchOptions
      }),

    resetAdminUserPassword: (id: string, newPassword: string) =>
      $fetch<unknown>(`/api/admin/users/${id}/password`, {
        method: 'PUT',
        body: { newPassword },
        headers: jsonHeaders,
        ...baseFetchOptions
      })
  }
}
