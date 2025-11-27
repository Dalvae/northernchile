export function useDateTime() {
  // Always use es-CL locale for consistent DD/MM/YYYY formatting
  const localeCode = 'es-CL'

  const formatDate = (value: string | number | Date): string => {
    if (!value) return ''
    const date = value instanceof Date ? value : new Date(value)
    if (Number.isNaN(date.getTime())) return ''

    return date.toLocaleDateString(localeCode, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }

  const formatDateTime = (value: string | number | Date): string => {
    if (!value) return ''
    const date = value instanceof Date ? value : new Date(value)
    if (Number.isNaN(date.getTime())) return ''

    const formatted = date.toLocaleString(localeCode, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })

    return formatted
  }

  const formatTime = (value: string | number | Date): string => {
    if (!value) return ''
    const date = value instanceof Date ? value : new Date(value)
    if (Number.isNaN(date.getTime())) return ''

    return date.toLocaleTimeString(localeCode, {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    })
  }

  return {
    formatDate,
    formatDateTime,
    formatTime
  }
}
