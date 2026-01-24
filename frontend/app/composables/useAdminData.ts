// Facade composable that combines all admin feature composables
// For backwards compatibility with existing imports
// New code can import feature-specific composables directly from ./admin/

import { useAdminTours } from './admin/useAdminTours'
import { useAdminSchedules } from './admin/useAdminSchedules'
import { useAdminBookings } from './admin/useAdminBookings'
import { useAdminUsers } from './admin/useAdminUsers'
import { useAdminMedia } from './admin/useAdminMedia'
import { useAdminReports } from './admin/useAdminReports'

// Re-export types for backwards compatibility
export type { PageAuditLogRes } from './admin/useAdminReports'

export const useAdminData = () => {
  return {
    ...useAdminTours(),
    ...useAdminSchedules(),
    ...useAdminBookings(),
    ...useAdminUsers(),
    ...useAdminMedia(),
    ...useAdminReports()
  }
}
