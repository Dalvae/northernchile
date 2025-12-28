/**
 * Shared utility functions for media management
 */

import type { BadgeColor } from '~/types/ui'
import { formatDisplayDate, formatDisplayDateTime } from '~/utils/dateUtils'

/**
 * Format file size in human-readable format.
 * @param bytes - File size in bytes
 * @param precision - Decimal places (default: 1)
 */
export function formatFileSize(bytes?: number, precision = 1): string {
  if (!bytes) return '-'
  const KB = 1024
  const MB = KB * 1024
  if (bytes >= MB) return `${(bytes / MB).toFixed(precision)} MB`
  if (bytes >= KB) return `${(bytes / KB).toFixed(precision)} KB`
  return `${bytes} bytes`
}

/**
 * Format date for display (uses centralized dateUtils).
 * @deprecated Use formatDisplayDate from dateUtils directly
 */
export function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  return formatDisplayDate(dateString)
}

/**
 * Format datetime for display (uses centralized dateUtils).
 * @deprecated Use formatDisplayDateTime from dateUtils directly
 */
export function formatDateTime(dateString?: string): string {
  if (!dateString) return '-'
  return formatDisplayDateTime(new Date(dateString))
}

export function getMediaTypeLabel(type?: string): string {
  const labels: Record<string, string> = {
    TOUR: 'Tour',
    SCHEDULE: 'Salida',
    LOOSE: 'Suelto'
  }
  return labels[type || ''] || type || '-'
}

export function getMediaTypeBadgeColor(type?: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    TOUR: 'primary',
    SCHEDULE: 'secondary',
    LOOSE: 'neutral'
  }
  return colors[type || ''] || 'neutral'
}

export function formatForDateTimeInput(isoString: string): string {
  // Convert ISO to datetime-local format
  const date = new Date(isoString)
  const offset = date.getTimezoneOffset() * 60000
  const localDate = new Date(date.getTime() - offset)
  return localDate.toISOString().slice(0, 16)
}
