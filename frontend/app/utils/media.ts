/**
 * Shared utility functions for media management
 */

export function formatFileSize(bytes?: number): string {
  if (!bytes) return '-'
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(1)} MB`
}

export function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('es-CL')
}

export function formatDateTime(dateString?: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('es-CL')
}

export function getMediaTypeLabel(type?: string): string {
  const labels: Record<string, string> = {
    TOUR: 'Tour',
    SCHEDULE: 'Salida',
    LOOSE: 'Suelto'
  }
  return labels[type || ''] || type || '-'
}

export function getMediaTypeBadgeColor(type?: string): string {
  const colors: Record<string, string> = {
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
