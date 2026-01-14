/**
 * Composable for handling form submissions with loading state and error handling.
 * Built on top of useApiError for consistent error handling.
 *
 * @example
 * const { loading, submit } = useFormSubmit()
 *
 * async function handleSubmit() {
 *   const result = await submit(
 *     () => $fetch('/api/users', { method: 'POST', body: userData }),
 *     { successMessage: t('user.created'), errorTitle: t('user.createError') }
 *   )
 *   if (result) {
 *     emit('success')
 *   }
 * }
 */
export function useFormSubmit() {
  const { showErrorToast, showSuccessToast, parseError } = useApiError()

  const loading = ref(false)
  const error = ref<ReturnType<typeof parseError> | null>(null)

  /**
   * Submit a form with automatic loading state and toast notifications.
   *
   * @param fn - Async function that performs the submission
   * @param options - Optional success/error messages
   * @returns The result of fn, or null if an error occurred
   */
  async function submit<T>(
    fn: () => Promise<T>,
    options?: {
      successMessage?: string
      successDescription?: string
      errorTitle?: string
      showSuccessToast?: boolean
      showErrorToast?: boolean
    }
  ): Promise<T | null> {
    const showSuccess = options?.showSuccessToast !== false
    const showError = options?.showErrorToast !== false

    loading.value = true
    error.value = null

    try {
      const result = await fn()

      if (showSuccess && options?.successMessage) {
        showSuccessToast(options.successMessage, options.successDescription)
      }

      return result
    } catch (e) {
      const apiError = parseError(e)
      error.value = apiError

      if (showError) {
        showErrorToast(e, options?.errorTitle)
      }

      return null
    } finally {
      loading.value = false
    }
  }

  /**
   * Reset the error state.
   */
  function clearError() {
    error.value = null
  }

  return {
    loading: readonly(loading),
    error: readonly(error),
    submit,
    clearError
  }
}
