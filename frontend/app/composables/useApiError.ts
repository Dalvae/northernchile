/**
 * Composable for centralized API error handling
 * Provides consistent error extraction and user-friendly messages
 *
 * Supports business error codes from the backend for i18n-based messages:
 * - SCHEDULE_FULL: Tour schedule has no availability
 * - TOUR_NOT_ACTIVE: Tour is not active or published
 * - BOOKING_CUTOFF_PASSED: Booking window has closed
 * - INVALID_BOOKING_STATE: Invalid booking state transition
 * - CART_EXPIRED: Cart has expired
 * - DUPLICATE_BOOKING: Duplicate booking attempt
 */

import logger from '~/utils/logger'

/**
 * Standard API error response from backend
 */
export interface ApiError {
  message: string
  statusCode?: number
  errorCode?: string // Business error code for i18n
  errors?: Record<string, string> // Validation errors
  timestamp?: string
  // Additional context fields from business exceptions
  availableSlots?: number
  hoursRequired?: number
  currentStatus?: string
  allowedTransitions?: string[]
}

/**
 * Backend error response structure
 */
interface BackendErrorData {
  message?: string
  error?: string
  status?: number
  statusCode?: number
  errorCode?: string
  errors?: Record<string, string>
  timestamp?: string
  availableSlots?: number
  hoursRequired?: number
  currentStatus?: string
  allowedTransitions?: string[]
}

/**
 * Nuxt/fetch error structure
 */
interface FetchError {
  data?: BackendErrorData
  message?: string
  statusMessage?: string
  statusCode?: number
  status?: number
}

/**
 * Type guard to check if error is a fetch error
 */
function isFetchError(error: unknown): error is FetchError {
  return typeof error === 'object' && error !== null && ('data' in error || 'statusCode' in error)
}

export function useApiError() {
  const { t, te } = useI18n()

  /**
   * Extract error message from unknown error object
   * Handles various error formats from the API
   */
  function extractErrorMessage(error: unknown): string {
    if (!error) {
      return t('common.error_unknown')
    }

    if (typeof error === 'string') {
      return error
    }

    if (isFetchError(error)) {
      // Check for data.message (most common from API)
      if (error.data?.message) {
        return error.data.message
      }

      // Check for message property
      if (error.message) {
        return error.message
      }

      // Check for statusMessage
      if (error.statusMessage) {
        return error.statusMessage
      }
    }

    return t('common.error_unknown')
  }

  /**
   * Extract status code from error object
   */
  function extractStatusCode(error: unknown): number | undefined {
    if (!isFetchError(error)) {
      return undefined
    }

    return error.statusCode ?? error.status ?? error.data?.status ?? error.data?.statusCode
  }

  /**
   * Extract business error code from error object
   */
  function extractErrorCode(error: unknown): string | undefined {
    if (!isFetchError(error)) {
      return undefined
    }

    return error.data?.errorCode
  }

  /**
   * Extract validation errors from error object
   * Used for form validation errors from backend
   */
  function extractValidationErrors(error: unknown): Record<string, string> | undefined {
    if (!isFetchError(error)) {
      return undefined
    }

    return error.data?.errors
  }

  /**
   * Extract additional context from business exceptions
   */
  function extractBusinessContext(error: unknown): Partial<ApiError> {
    if (!isFetchError(error)) {
      return {}
    }

    return {
      availableSlots: error.data?.availableSlots,
      hoursRequired: error.data?.hoursRequired,
      currentStatus: error.data?.currentStatus,
      allowedTransitions: error.data?.allowedTransitions
    }
  }

  /**
   * Parse unknown error into structured ApiError object
   */
  function parseError(error: unknown): ApiError {
    const businessContext = extractBusinessContext(error)

    return {
      message: extractErrorMessage(error),
      statusCode: extractStatusCode(error),
      errorCode: extractErrorCode(error),
      errors: extractValidationErrors(error),
      timestamp: new Date().toISOString(),
      ...businessContext
    }
  }

  /**
   * Get user-friendly message based on error code (business errors)
   * Falls back to status code-based messages
   */
  function getErrorMessage(apiError: ApiError): string {
    // First, try to get a message based on the business error code
    if (apiError.errorCode) {
      const errorCodeKey = `errors.business.${apiError.errorCode.toLowerCase()}`
      if (te(errorCodeKey)) {
        // Use interpolation for dynamic values
        return t(errorCodeKey, {
          availableSlots: apiError.availableSlots,
          hoursRequired: apiError.hoursRequired,
          currentStatus: apiError.currentStatus
        })
      }
    }

    // Fall back to status code-based message
    return getFriendlyMessage(apiError.statusCode)
  }

  /**
   * Get user-friendly message based on status code
   */
  function getFriendlyMessage(statusCode?: number): string {
    switch (statusCode) {
      case 400:
        return t('errors.bad_request')
      case 401:
        return t('errors.unauthorized')
      case 402:
        return t('errors.payment_required', 'Pago requerido')
      case 403:
        return t('errors.forbidden')
      case 404:
        return t('errors.not_found')
      case 409:
        return t('errors.conflict')
      case 410:
        return t('errors.gone', 'El recurso ya no está disponible')
      case 422:
        return t('errors.validation_failed')
      case 500:
        return t('errors.server_error')
      case 502:
        return t('errors.bad_gateway', 'Error de servicio externo')
      case 503:
        return t('errors.service_unavailable')
      default:
        return t('common.error_unknown')
    }
  }

  /**
   * Show error toast with proper formatting
   * Uses error code for title and detailed message for description
   */
  function showErrorToast(error: unknown, fallbackTitle?: string) {
    const toast = useToast()
    const apiError = parseError(error)

    const title = fallbackTitle || getErrorMessage(apiError)
    const description = apiError.message !== title ? apiError.message : undefined

    toast.add({
      color: 'error',
      title,
      description
    })

    // Log validation errors if present
    if (apiError.errors) {
      logger.error('Validation errors:', apiError.errors)
    }
  }

  /**
   * Show success toast
   */
  function showSuccessToast(title: string, description?: string) {
    const toast = useToast()
    toast.add({
      color: 'success',
      title,
      description
    })
  }

  /**
   * Handle error in async operations with automatic toast
   * Returns the parsed error for further processing if needed
   */
  async function handleAsyncError<T>(
    operation: () => Promise<T>,
    options?: {
      fallbackTitle?: string
      showToast?: boolean
      onError?: (error: ApiError) => void
    }
  ): Promise<{ data: T, error: null } | { data: null, error: ApiError }> {
    const { fallbackTitle, showToast = true, onError } = options ?? {}

    try {
      const data = await operation()
      return { data, error: null }
    } catch (e) {
      const apiError = parseError(e)

      if (showToast) {
        showErrorToast(e, fallbackTitle)
      }

      onError?.(apiError)

      return { data: null, error: apiError }
    }
  }

  /**
   * Check if error is a specific HTTP status code
   */
  function isStatusCode(error: unknown, code: number): boolean {
    return extractStatusCode(error) === code
  }

  /**
   * Check if error is a specific business error code
   */
  function isErrorCode(error: unknown, code: string): boolean {
    return extractErrorCode(error) === code
  }

  /**
   * Check if error is an authentication error (401)
   */
  function isAuthError(error: unknown): boolean {
    return isStatusCode(error, 401)
  }

  /**
   * Check if error is a not found error (404)
   */
  function isNotFoundError(error: unknown): boolean {
    return isStatusCode(error, 404)
  }

  /**
   * Check if error is a validation error (400 or 422)
   */
  function isValidationError(error: unknown): boolean {
    const code = extractStatusCode(error)
    return code === 400 || code === 422
  }

  /**
   * Check if error is a conflict error (409)
   * Common for business rule violations
   */
  function isConflictError(error: unknown): boolean {
    return isStatusCode(error, 409)
  }

  /**
   * Check if error indicates resource is gone/expired (410)
   */
  function isGoneError(error: unknown): boolean {
    return isStatusCode(error, 410)
  }

  /**
   * Check if schedule is full
   */
  function isScheduleFullError(error: unknown): boolean {
    return isErrorCode(error, 'SCHEDULE_FULL')
  }

  /**
   * Check if booking window has closed
   */
  function isBookingCutoffError(error: unknown): boolean {
    return isErrorCode(error, 'BOOKING_CUTOFF_PASSED')
  }

  return {
    // Core functions
    extractErrorMessage,
    extractStatusCode,
    extractErrorCode,
    extractValidationErrors,
    parseError,
    getFriendlyMessage,
    getErrorMessage,

    // Toast helpers
    showErrorToast,
    showSuccessToast,

    // Async handler
    handleAsyncError,

    // Type checks
    isStatusCode,
    isErrorCode,
    isAuthError,
    isNotFoundError,
    isValidationError,
    isConflictError,
    isGoneError,
    isScheduleFullError,
    isBookingCutoffError
  }
}
