/**
 * Composable for managing modal form state and submission
 * Abstracts common patterns from admin modals: open/close state,
 * loading state, and async submission with toast notifications
 *
 * @example Basic usage
 * ```ts
 * const { isOpen, isSubmitting, open, close, handleSubmit } = useModalForm({
 *   onSubmit: async () => {
 *     await api.createUser(formData)
 *   },
 *   onSuccess: () => {
 *     emit('success')
 *   },
 *   successMessage: 'Usuario creado'
 * })
 * ```
 *
 * @example With form reset
 * ```ts
 * const { isOpen, isSubmitting, handleSubmit } = useModalForm({
 *   onSubmit: async () => {
 *     await api.updateUser(userId, formData)
 *     return { userName: formData.name } // Return data for success message
 *   },
 *   onSuccess: (data) => {
 *     resetForm()
 *     emit('success')
 *   },
 *   successMessage: (data) => `Usuario ${data?.userName} actualizado`
 * })
 * ```
 *
 * @example With error callback
 * ```ts
 * const { handleSubmit } = useModalForm({
 *   onSubmit: async () => { ... },
 *   onError: (error) => {
 *     if (error.statusCode === 409) {
 *       // Handle conflict
 *     }
 *   },
 *   errorMessage: 'Error al guardar'
 * })
 * ```
 */

import type { ApiError } from './useApiError'

/**
 * Options for configuring the modal form behavior
 */
export interface UseModalFormOptions<TResult = void> {
  /**
   * Async function to execute on form submission
   * Can return data that will be passed to onSuccess
   */
  onSubmit: () => Promise<TResult>

  /**
   * Callback executed after successful submission
   * Receives the result from onSubmit
   */
  onSuccess?: (result: TResult) => void

  /**
   * Callback executed when submission fails
   * Receives the parsed API error
   */
  onError?: (error: ApiError) => void

  /**
   * Message to show in success toast
   * Can be a string or a function that receives the result
   */
  successMessage?: string | ((result: TResult) => string)

  /**
   * Fallback title for error toast (uses API error message if available)
   */
  errorMessage?: string

  /**
   * Whether to show success toast (default: true if successMessage provided)
   */
  showSuccessToast?: boolean

  /**
   * Whether to show error toast (default: true)
   */
  showErrorToast?: boolean

  /**
   * Whether to close modal on success (default: true)
   */
  closeOnSuccess?: boolean

  /**
   * Optional validation function that runs before onSubmit
   * Return false or throw to prevent submission
   */
  validate?: () => boolean | Promise<boolean>
}

/**
 * Return type for useModalForm composable
 */
export interface UseModalFormReturn<TResult = void> {
  /**
   * Whether the modal is open
   */
  isOpen: Ref<boolean>

  /**
   * Whether form is currently submitting
   */
  isSubmitting: Ref<boolean>

  /**
   * Open the modal
   */
  open: () => void

  /**
   * Close the modal
   */
  close: () => void

  /**
   * Toggle modal open state
   */
  toggle: () => void

  /**
   * Handle form submission with loading state and error handling
   * Returns the result on success, null on failure
   */
  handleSubmit: () => Promise<TResult | null>

  /**
   * Reset the modal state (close and reset submitting)
   */
  reset: () => void
}

/**
 * Composable for managing modal form state and submission
 */
export function useModalForm<TResult = void>(
  options: UseModalFormOptions<TResult>
): UseModalFormReturn<TResult> {
  const {
    onSubmit,
    onSuccess,
    onError,
    successMessage,
    errorMessage,
    showSuccessToast: showSuccess = !!successMessage,
    showErrorToast: showError = true,
    closeOnSuccess = true,
    validate
  } = options

  const { showSuccessToast, showErrorToast, parseError } = useApiError()

  // State
  const isOpen = ref(false)
  const isSubmitting = ref(false)

  /**
   * Open the modal
   */
  function open() {
    isOpen.value = true
  }

  /**
   * Close the modal
   */
  function close() {
    isOpen.value = false
  }

  /**
   * Toggle modal open state
   */
  function toggle() {
    isOpen.value = !isOpen.value
  }

  /**
   * Reset modal state
   */
  function reset() {
    isOpen.value = false
    isSubmitting.value = false
  }

  /**
   * Handle form submission with loading state, validation, and error handling
   */
  async function handleSubmit(): Promise<TResult | null> {
    // Run validation if provided
    if (validate) {
      try {
        const isValid = await validate()
        if (!isValid) {
          return null
        }
      } catch {
        return null
      }
    }

    isSubmitting.value = true

    try {
      const result = await onSubmit()

      // Show success toast
      if (showSuccess && successMessage) {
        const message = typeof successMessage === 'function'
          ? successMessage(result)
          : successMessage
        showSuccessToast(message)
      }

      // Close modal on success
      if (closeOnSuccess) {
        isOpen.value = false
      }

      // Call success callback
      onSuccess?.(result)

      return result
    } catch (error) {
      const apiError = parseError(error)

      // Show error toast
      if (showError) {
        showErrorToast(error, errorMessage)
      }

      // Call error callback
      onError?.(apiError)

      return null
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    isOpen,
    isSubmitting,
    open,
    close,
    toggle,
    handleSubmit,
    reset
  }
}

/**
 * Options for creating a modal form with external open state (v-model pattern)
 */
export interface UseControlledModalFormOptions<TResult = void>
  extends Omit<UseModalFormOptions<TResult>, 'closeOnSuccess'> {
  /**
   * Model value ref for v-model:open pattern
   */
  modelValue: Ref<boolean>

  /**
   * Emit function to update v-model
   */
  onUpdateModelValue: (value: boolean) => void
}

/**
 * Return type for controlled modal form
 */
export interface UseControlledModalFormReturn<TResult = void>
  extends Omit<UseModalFormReturn<TResult>, 'isOpen'> {
  /**
   * Computed ref for two-way binding with v-model
   */
  isOpen: WritableComputedRef<boolean>
}

/**
 * Composable for modal forms with external state control (v-model pattern)
 * Use this when the parent component controls the open state
 *
 * @example
 * ```ts
 * const props = defineProps<{ modelValue: boolean }>()
 * const emit = defineEmits(['update:modelValue', 'success'])
 *
 * const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
 *   modelValue: toRef(props, 'modelValue'),
 *   onUpdateModelValue: (v) => emit('update:modelValue', v),
 *   onSubmit: async () => { ... },
 *   successMessage: 'Guardado'
 * })
 * ```
 */
export function useControlledModalForm<TResult = void>(
  options: UseControlledModalFormOptions<TResult>
): UseControlledModalFormReturn<TResult> {
  const {
    modelValue,
    onUpdateModelValue,
    onSubmit,
    onSuccess,
    onError,
    successMessage,
    errorMessage,
    showSuccessToast: showSuccess = !!successMessage,
    showErrorToast: showError = true,
    validate
  } = options

  const { showSuccessToast, showErrorToast, parseError } = useApiError()

  // Computed for v-model binding
  const isOpen = computed({
    get: () => modelValue.value,
    set: (value: boolean) => onUpdateModelValue(value)
  })

  const isSubmitting = ref(false)

  function open() {
    isOpen.value = true
  }

  function close() {
    isOpen.value = false
  }

  function toggle() {
    isOpen.value = !isOpen.value
  }

  function reset() {
    isOpen.value = false
    isSubmitting.value = false
  }

  async function handleSubmit(): Promise<TResult | null> {
    if (validate) {
      try {
        const isValid = await validate()
        if (!isValid) {
          return null
        }
      } catch {
        return null
      }
    }

    isSubmitting.value = true

    try {
      const result = await onSubmit()

      if (showSuccess && successMessage) {
        const message = typeof successMessage === 'function'
          ? successMessage(result)
          : successMessage
        showSuccessToast(message)
      }

      // Close modal on success
      isOpen.value = false

      onSuccess?.(result)

      return result
    } catch (error) {
      const apiError = parseError(error)

      if (showError) {
        showErrorToast(error, errorMessage)
      }

      onError?.(apiError)

      return null
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    isOpen,
    isSubmitting,
    open,
    close,
    toggle,
    handleSubmit,
    reset
  }
}
