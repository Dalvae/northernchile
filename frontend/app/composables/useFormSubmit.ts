/**
 * Composable for handling form submissions with loading state and error handling.
 *
 * @example
 * const { loading, submit } = useFormSubmit()
 *
 * async function handleSubmit() {
 *   const result = await submit(
 *     () => $fetch('/api/users', { method: 'POST', body: userData }),
 *     { successMessage: t('user.created'), errorMessage: t('user.createError') }
 *   )
 *   if (result) {
 *     emit('success')
 *   }
 * }
 */
export function useFormSubmit() {
  const toast = useToast()
  const { t } = useI18n()
  const { extractErrorMessage } = useApiError()

  const loading = ref(false)
  const error = ref<Error | null>(null)

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
      errorMessage?: string
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
        toast.add({
          title: options.successMessage,
          color: 'success',
          icon: 'i-heroicons-check-circle'
        })
      }

      return result
    } catch (e) {
      error.value = e as Error
      const errorMsg = extractErrorMessage(e)

      if (showError) {
        toast.add({
          title: options?.errorMessage || t('common.error'),
          description: errorMsg,
          color: 'error',
          icon: 'i-heroicons-exclamation-triangle'
        })
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
