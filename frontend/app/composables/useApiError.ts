/**
 * Composable for centralized API error handling
 * Provides consistent error extraction and user-friendly messages
 */

export interface ApiError {
  message: string
  statusCode?: number
  errors?: Record<string, string> // Validation errors
  timestamp?: string
}

export function useApiError() {
  const { t } = useI18n()

  /**
   * Extract error message from unknown error object
   * Handles various error formats from the API
   */
  function extractErrorMessage(error: unknown): string {
    // Handle null/undefined
    if (!error) {
      return t('common.error_unknown')
    }

    // String error
    if (typeof error === 'string') {
      return error
    }

    // Object error
    if (typeof error === 'object') {
      const err = error as Record<string, unknown>

      // Check for data.message (most common from API)
      if (err.data && typeof err.data === 'object') {
        const data = err.data as Record<string, unknown>
        if (data.message && typeof data.message === 'string') {
          return data.message
        }
      }

      // Check for message property
      if (err.message && typeof err.message === 'string') {
        return err.message
      }

      // Check for error property
      if (err.error && typeof err.error === 'string') {
        return err.error
      }

      // Check for statusMessage
      if (err.statusMessage && typeof err.statusMessage === 'string') {
        return err.statusMessage
      }
    }

    return t('common.error_unknown')
  }

  /**
   * Extract status code from error object
   */
  function extractStatusCode(error: unknown): number | undefined {
    if (!error || typeof error !== 'object') {
      return undefined
    }

    const err = error as Record<string, unknown>

    // Check statusCode property
    if (typeof err.statusCode === 'number') {
      return err.statusCode
    }

    // Check status property
    if (typeof err.status === 'number') {
      return err.status
    }

    // Check data.status
    if (err.data && typeof err.data === 'object') {
      const data = err.data as Record<string, unknown>
      if (typeof data.status === 'number') {
        return data.status
      }
    }

    return undefined
  }

  /**
   * Extract validation errors from error object
   * Used for form validation errors from backend
   */
  function extractValidationErrors(error: unknown): Record<string, string> | undefined {
    if (!error || typeof error !== 'object') {
      return undefined
    }

    const err = error as Record<string, unknown>

    // Check for errors property (from backend ValidationErrorResponse)
    if (err.data && typeof err.data === 'object') {
      const data = err.data as Record<string, unknown>
      if (data.errors && typeof data.errors === 'object') {
        return data.errors as Record<string, string>
      }
    }

    // Check for errors directly on error object
    if (err.errors && typeof err.errors === 'object') {
      return err.errors as Record<string, string>
    }

    return undefined
  }

  /**
   * Parse unknown error into structured ApiError object
   */
  function parseError(error: unknown): ApiError {
    return {
      message: extractErrorMessage(error),
      statusCode: extractStatusCode(error),
      errors: extractValidationErrors(error),
      timestamp: new Date().toISOString()
    }
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
      case 403:
        return t('errors.forbidden')
      case 404:
        return t('errors.not_found')
      case 409:
        return t('errors.conflict')
      case 422:
        return t('errors.validation_failed')
      case 500:
        return t('errors.server_error')
      case 503:
        return t('errors.service_unavailable')
      default:
        return t('common.error_unknown')
    }
  }

  /**
   * Show error toast with proper formatting
   */
  function showErrorToast(error: unknown, fallbackTitle?: string) {
    const toast = useToast()
    const apiError = parseError(error)

    toast.add({
      color: 'error',
      title: fallbackTitle || getFriendlyMessage(apiError.statusCode),
      description: apiError.message,
      timeout: 5000
    })

    // Log validation errors if present
    if (apiError.errors) {
      console.error('Validation errors:', apiError.errors)
    }
  }

  /**
   * Check if error is a specific HTTP status code
   */
  function isStatusCode(error: unknown, code: number): boolean {
    return extractStatusCode(error) === code
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

  return {
    extractErrorMessage,
    extractStatusCode,
    extractValidationErrors,
    parseError,
    getFriendlyMessage,
    showErrorToast,
    isStatusCode,
    isAuthError,
    isNotFoundError,
    isValidationError
  }
}
