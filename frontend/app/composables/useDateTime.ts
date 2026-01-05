import {
  formatDisplayDate,
  formatDisplayDateLong,
  formatDisplayDateTime,
  formatDisplayTime,
  parseDateOnly,
  getLocalDateString,
  getTodayString,
  isToday,
  isPastDate,
  isFutureDate,
  isTodayOrFuture,
  addDays,
  toInputDateFormat,
  CHILE_TIMEZONE
} from '~/utils/dateUtils'
import type { LocalTime } from 'api-client'

/**
 * Composable for date/time formatting and manipulation.
 *
 * Uses Chilean timezone (America/Santiago) and DD-MM-YYYY display format.
 * All date strings from backend are expected in YYYY-MM-DD (ISO) format.
 */
export function useDateTime() {
  /**
   * Format a date for display (DD-MM-YYYY).
   * Accepts Date object or YYYY-MM-DD string.
   */
  const formatDate = (value: string | number | Date): string => {
    if (!value) return ''

    // Handle YYYY-MM-DD strings from backend
    if (typeof value === 'string' && !value.includes('T')) {
      return formatDisplayDate(value)
    }

    // Handle Date objects and ISO datetime strings
    const date = value instanceof Date ? value : new Date(value)
    return formatDisplayDate(date)
  }

  /**
   * Format a date with full month name (e.g., "15 de enero de 2025").
   */
  const formatDateLong = (value: string | number | Date): string => {
    if (!value) return ''

    if (typeof value === 'string' && !value.includes('T')) {
      return formatDisplayDateLong(value)
    }

    const date = value instanceof Date ? value : new Date(value)
    return formatDisplayDateLong(date)
  }

  /**
   * Format a datetime for display (DD-MM-YYYY HH:mm).
   */
  const formatDateTime = (value: string | number | Date): string => {
    if (!value) return ''

    const date = value instanceof Date ? value : new Date(value)
    return formatDisplayDateTime(date)
  }

  /**
   * Format time only (HH:mm).
   */
  const formatTime = (value: string | number | Date): string => {
    if (!value) return ''

    const date = value instanceof Date ? value : new Date(value)
    return formatDisplayTime(date)
  }

  /**
   * Format a LocalTime object or string (HH:mm).
   */
  const formatLocalTime = (value: LocalTime | string | null | undefined): string => {
    if (!value) return ''
    if (typeof value === 'string') {
      return value.slice(0, 5)
    }
    if (typeof value === 'object' && 'hour' in value && 'minute' in value) {
      const h = String(value.hour).padStart(2, '0')
      const m = String(value.minute).padStart(2, '0')
      return `${h}:${m}`
    }
    return ''
  }

  /**
   * Format a date for localized display based on current locale.
   * Falls back to Chilean format.
   */
  const formatLocalized = (
    value: string | number | Date,
    options?: Intl.DateTimeFormatOptions
  ): string => {
    if (!value) return ''

    const date = typeof value === 'string'
      ? (value.includes('T') ? new Date(value) : parseDateOnly(value))
      : value instanceof Date
        ? value
        : new Date(value)

    if (Number.isNaN(date.getTime())) return ''

    const defaultOptions: Intl.DateTimeFormatOptions = {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      timeZone: CHILE_TIMEZONE
    }

    return date.toLocaleDateString('es-CL', { ...defaultOptions, ...options })
  }

  return {
    // Formatting
    formatDate,
    formatDateLong,
    formatDateTime,
    formatTime,
    formatLocalTime,
    formatLocalized,

    // Re-export utilities for convenience
    parseDateOnly,
    getLocalDateString,
    getTodayString,
    toInputDateFormat,

    // Date checks
    isToday,
    isPastDate,
    isFutureDate,
    isTodayOrFuture,

    // Manipulation
    addDays,

    // Constants
    CHILE_TIMEZONE
  }
}
