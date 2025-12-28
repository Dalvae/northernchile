/**
 * Centralized admin options to avoid duplication across components.
 * Use these constants for dropdowns, filters, and status badges.
 */

import type { BadgeColor } from '~/types/ui'

// Re-export for convenience
export type { BadgeColor } from '~/types/ui'

// User roles
export const USER_ROLE_OPTIONS = [
  { label: 'Cliente', value: 'ROLE_CLIENT' },
  { label: 'Partner Admin', value: 'ROLE_PARTNER_ADMIN' },
  { label: 'Super Admin', value: 'ROLE_SUPER_ADMIN' }
] as const

export const USER_ROLE_FILTER_OPTIONS = [
  { label: 'Todos', value: 'ALL' },
  ...USER_ROLE_OPTIONS
] as const

// Tour status
export const TOUR_STATUS_OPTIONS = [
  { label: 'Borrador', value: 'DRAFT' },
  { label: 'Publicado', value: 'PUBLISHED' },
  { label: 'Archivado', value: 'ARCHIVED' }
] as const

// Schedule status
export const SCHEDULE_STATUS_OPTIONS = [
  { label: 'Abierto', value: 'OPEN' },
  { label: 'Cerrado', value: 'CLOSED' },
  { label: 'Cancelado', value: 'CANCELLED' }
] as const

export const SCHEDULE_STATUS_FILTER_OPTIONS = [
  { label: 'Todos', value: 'ALL' },
  ...SCHEDULE_STATUS_OPTIONS
] as const

// Booking status
export const BOOKING_STATUS_OPTIONS = [
  { label: 'Confirmada', value: 'CONFIRMED' },
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Cancelada', value: 'CANCELLED' },
  { label: 'Completada', value: 'COMPLETED' }
] as const

export const BOOKING_STATUS_FILTER_OPTIONS = [
  { label: 'Todos', value: 'ALL' },
  ...BOOKING_STATUS_OPTIONS
] as const

// Private tour request status
export const PRIVATE_REQUEST_STATUS_OPTIONS = [
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Cotizado', value: 'QUOTED' },
  { label: 'Confirmado', value: 'CONFIRMED' },
  { label: 'Cancelado', value: 'CANCELLED' }
] as const

// Alert status
export const ALERT_STATUS_OPTIONS = [
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Reconocida', value: 'ACKNOWLEDGED' },
  { label: 'Resuelta', value: 'RESOLVED' }
] as const

// Badge colors for statuses
const STATUS_COLORS: Record<string, BadgeColor> = {
  // Booking status
  CONFIRMED: 'success',
  PENDING: 'warning',
  CANCELLED: 'error',
  COMPLETED: 'info',
  // Schedule status
  OPEN: 'success',
  CLOSED: 'neutral',
  // Tour status
  DRAFT: 'warning',
  PUBLISHED: 'success',
  ARCHIVED: 'neutral',
  // Private request status
  QUOTED: 'info',
  // Alert status
  ACKNOWLEDGED: 'info',
  RESOLVED: 'success',
  // User roles
  ROLE_CLIENT: 'neutral',
  ROLE_PARTNER_ADMIN: 'info',
  ROLE_SUPER_ADMIN: 'primary'
}

export function getStatusColor(status: string): BadgeColor {
  return STATUS_COLORS[status] || 'neutral'
}

export function getStatusLabel(status: string, options: readonly { label: string, value: string }[]): string {
  const option = options.find(opt => opt.value === status)
  return option?.label || status
}

// Convenience functions for specific status types
export function getBookingStatusLabel(status: string): string {
  return getStatusLabel(status, BOOKING_STATUS_OPTIONS)
}

export function getScheduleStatusLabel(status: string): string {
  return getStatusLabel(status, SCHEDULE_STATUS_OPTIONS)
}

export function getTourStatusLabel(status: string): string {
  return getStatusLabel(status, TOUR_STATUS_OPTIONS)
}

export function getUserRoleLabel(status: string): string {
  return getStatusLabel(status, USER_ROLE_OPTIONS)
}

export function getPrivateRequestStatusLabel(status: string): string {
  return getStatusLabel(status, PRIVATE_REQUEST_STATUS_OPTIONS)
}
