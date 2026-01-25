/**
 * Date Utilities for Northern Chile
 *
 * This module provides timezone-safe date handling utilities.
 * All tours operate in Chile timezone (America/Santiago).
 *
 * KEY PRINCIPLES:
 * 1. Backend sends LocalDate as "YYYY-MM-DD" strings - keep them as strings
 * 2. Backend sends Instant as ISO-8601 with Z suffix - parse with new Date()
 * 3. Display format is DD-MM-YYYY (Chilean standard, not US format)
 * 4. Never use toISOString().split('T')[0] - it returns UTC date, not local
 * 5. For date comparisons, compare strings in YYYY-MM-DD format
 */

// Chile timezone
export const CHILE_TIMEZONE = 'America/Santiago'

/**
 * Convert an Instant (UTC timestamp) to Chile local time string for FullCalendar.
 *
 * FullCalendar uses the date portion of the ISO string to determine which calendar day
 * to show the event, even when timeZone is configured. This function converts UTC to
 * local time BEFORE passing to FullCalendar to ensure correct day placement.
 *
 * @example
 * // Backend sends: "2026-01-24T00:02:00Z" (UTC)
 * // In Chile this is: "2026-01-23T21:02:00" (day 23, not 24)
 * instantToChileLocalString("2026-01-24T00:02:00Z")
 * // Returns: "2026-01-23T21:02:00"
 */
export function instantToChileLocalString(utcInstant: string): string {
  const date = new Date(utcInstant)

  // Format as ISO string in Chile timezone
  return date.toLocaleString('sv-SE', {
    timeZone: CHILE_TIMEZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(' ', 'T')
}

/**
 * Get today's date as YYYY-MM-DD string in local timezone.
 * Use this instead of: new Date().toISOString().split('T')[0]
 */
export function getTodayString(): string {
  return getLocalDateString(new Date())
}

/**
 * Convert a Date object to YYYY-MM-DD string in local timezone.
 * Use this instead of: date.toISOString().split('T')[0]
 *
 * @example
 * // At 11PM Chile time (which is next day in UTC)
 * getLocalDateString(new Date()) // Returns today's date, not tomorrow's
 */
export function getLocalDateString(date: Date): string {
  // Use sv-SE locale for YYYY-MM-DD format in Chile timezone
  return date.toLocaleDateString('sv-SE', { timeZone: CHILE_TIMEZONE })
}

/**
 * Parse a date-only string (YYYY-MM-DD) as local midnight.
 * Use this instead of: new Date(dateString)
 *
 * The problem with new Date('2025-01-15'):
 * - It's interpreted as UTC midnight
 * - In Chile (UTC-3), this shows as 21:00 on Jan 14
 *
 * @example
 * parseDateOnly('2025-01-15') // Returns Jan 15 at 00:00 local time
 */
export function parseDateOnly(dateStr: string): Date {
  if (!dateStr) return new Date(NaN)

  // If it already has a time component, parse normally
  if (dateStr.includes('T')) {
    return new Date(dateStr)
  }

  // Append T00:00:00 to interpret as local midnight
  return new Date(dateStr + 'T00:00:00')
}

/**
 * Parse an ISO datetime string (with Z or timezone offset).
 * Use for backend Instant fields (createdAt, startDatetime, etc.)
 */
export function parseInstant(isoString: string): Date {
  return new Date(isoString)
}

/**
 * Format a date for display in Chilean format (DD-MM-YYYY).
 *
 * @param date - Date object or YYYY-MM-DD string
 * @param options - Optional Intl.DateTimeFormat options
 */
export function formatDisplayDate(
  date: Date | string,
  options?: Intl.DateTimeFormatOptions
): string {
  if (!date) return ''

  const dateObj = typeof date === 'string' ? parseDateOnly(date) : date
  if (Number.isNaN(dateObj.getTime())) return ''

  const defaultOptions: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    timeZone: CHILE_TIMEZONE
  }

  return dateObj.toLocaleDateString('es-CL', { ...defaultOptions, ...options })
}

/**
 * Format a date for display with month name (e.g., "15 de enero de 2025").
 */
export function formatDisplayDateLong(date: Date | string): string {
  if (!date) return ''

  const dateObj = typeof date === 'string' ? parseDateOnly(date) : date
  if (Number.isNaN(dateObj.getTime())) return ''

  return dateObj.toLocaleDateString('es-CL', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: CHILE_TIMEZONE
  })
}

/**
 * Format a datetime for display (DD-MM-YYYY HH:mm).
 *
 * @param datetime - ISO datetime string or Date object
 */
export function formatDisplayDateTime(datetime: Date | string): string {
  if (!datetime) return ''

  const dateObj = typeof datetime === 'string' ? new Date(datetime) : datetime
  if (Number.isNaN(dateObj.getTime())) return ''

  return dateObj.toLocaleString('es-CL', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: CHILE_TIMEZONE
  })
}

/**
 * Format time only (HH:mm).
 */
export function formatDisplayTime(datetime: Date | string): string {
  if (!datetime) return ''

  const dateObj = typeof datetime === 'string' ? new Date(datetime) : datetime
  if (Number.isNaN(dateObj.getTime())) return ''

  return dateObj.toLocaleTimeString('es-CL', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZone: CHILE_TIMEZONE
  })
}

/**
 * Compare two date strings (YYYY-MM-DD format).
 * Returns: -1 if a < b, 0 if equal, 1 if a > b
 */
export function compareDateStrings(a: string, b: string): number {
  if (a < b) return -1
  if (a > b) return 1
  return 0
}

/**
 * Check if a date string is today.
 */
export function isToday(dateStr: string): boolean {
  return dateStr === getTodayString()
}

/**
 * Check if a date string is in the past (before today).
 */
export function isPastDate(dateStr: string): boolean {
  return dateStr < getTodayString()
}

/**
 * Check if a date string is in the future (after today).
 */
export function isFutureDate(dateStr: string): boolean {
  return dateStr > getTodayString()
}

/**
 * Check if a date string is today or in the future.
 */
export function isTodayOrFuture(dateStr: string): boolean {
  return dateStr >= getTodayString()
}

/**
 * Add days to a date and return as YYYY-MM-DD string.
 */
export function addDays(dateStr: string, days: number): string {
  const date = parseDateOnly(dateStr)
  date.setDate(date.getDate() + days)
  return getLocalDateString(date)
}

/**
 * Get the start of month for a given date.
 */
export function getMonthStart(year: number, month: number): string {
  return `${year}-${String(month + 1).padStart(2, '0')}-01`
}

/**
 * Get the end of month for a given date.
 */
export function getMonthEnd(year: number, month: number): string {
  // Day 0 of next month = last day of current month
  const lastDay = new Date(year, month + 1, 0).getDate()
  return `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
}

/**
 * Create an Instant (ISO datetime with Z) from a Chile date and time.
 * Use when sending datetime to backend.
 *
 * IMPORTANT: This function explicitly handles Chile timezone (America/Santiago)
 * regardless of where the code runs (local dev, Vercel UTC server, etc.)
 *
 * @param dateStr - YYYY-MM-DD string (Chile date)
 * @param timeStr - HH:mm or HH:mm:ss string (Chile time)
 */
export function createInstant(dateStr: string, timeStr: string): string {
  // Ensure time has seconds
  const normalizedTime = timeStr.length === 5 ? `${timeStr}:00` : timeStr

  // Parse the date/time components
  const [year, month, day] = dateStr.split('-').map(Number)
  const [hours, minutes, seconds] = normalizedTime.split(':').map(Number)

  // Get Chile's UTC offset for this specific date/time
  // We create a reference date and use Intl to find the offset
  const referenceDate = new Date(Date.UTC(year!, month! - 1, day!, 12, 0, 0))

  // Format the reference date in Chile timezone to find the offset
  const chileFormatter = new Intl.DateTimeFormat('en-US', {
    timeZone: CHILE_TIMEZONE,
    hour: '2-digit',
    hour12: false
  })
  const chileHour = Number(chileFormatter.format(referenceDate))

  // Calculate offset: if UTC 12:00 = Chile 09:00, offset is -3
  const offsetHours = 12 - chileHour

  // Create the correct UTC time by adding the offset
  const utcDate = new Date(Date.UTC(
    year!,
    month! - 1,
    day!,
    hours! + offsetHours,
    minutes!,
    seconds!
  ))

  return utcDate.toISOString()
}

/**
 * Get a Date object for the start of today in local timezone.
 */
export function getStartOfToday(): Date {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return now
}

/**
 * Get a Date object for the end of today in local timezone.
 */
export function getEndOfToday(): Date {
  const now = new Date()
  now.setHours(23, 59, 59, 999)
  return now
}

/**
 * Convert a Unix timestamp (seconds) to YYYY-MM-DD in local timezone.
 * Useful for weather API responses.
 */
export function unixToDateString(unixSeconds: number): string {
  return getLocalDateString(new Date(unixSeconds * 1000))
}

/**
 * Format a date for HTML date input (YYYY-MM-DD).
 * HTML date inputs require this format regardless of display preferences.
 */
export function toInputDateFormat(date: Date | string): string {
  if (!date) return ''

  if (typeof date === 'string') {
    // If already in YYYY-MM-DD format, return as-is
    if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      return date
    }
    return getLocalDateString(parseDateOnly(date))
  }

  return getLocalDateString(date)
}
