/**
 * Composable for managing a confirmation dialog with promise-based API.
 *
 * @example
 * const { confirm, ConfirmDialogProps, handleConfirm, handleCancel } = useConfirmDialog()
 *
 * // In your component:
 * async function deleteItem() {
 *   const confirmed = await confirm({
 *     title: 'Eliminar elemento',
 *     message: '¿Estás seguro de que deseas eliminar este elemento?',
 *     confirmLabel: 'Eliminar',
 *     confirmColor: 'error',
 *     icon: 'i-lucide-trash-2'
 *   })
 *
 *   if (confirmed) {
 *     // Perform deletion
 *   }
 * }
 *
 * // In template:
 * <CommonConfirmDialog v-bind="ConfirmDialogProps" @confirm="handleConfirm" @cancel="handleCancel" />
 */
export interface ConfirmOptions {
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  confirmColor?: 'primary' | 'error' | 'warning'
  icon?: string
}

export function useConfirmDialog() {
  const isOpen = ref(false)
  const loading = ref(false)
  const options = ref<ConfirmOptions>({
    title: '',
    message: ''
  })

  let resolvePromise: ((value: boolean) => void) | null = null

  /**
   * Show a confirmation dialog and wait for user response.
   * @returns Promise that resolves to true if confirmed, false if cancelled
   */
  function confirm(opts: ConfirmOptions): Promise<boolean> {
    options.value = opts
    isOpen.value = true

    return new Promise((resolve) => {
      resolvePromise = resolve
    })
  }

  /**
   * Handle the confirm action
   */
  function handleConfirm() {
    isOpen.value = false
    resolvePromise?.(true)
    resolvePromise = null
  }

  /**
   * Handle the cancel action
   */
  function handleCancel() {
    isOpen.value = false
    resolvePromise?.(false)
    resolvePromise = null
  }

  /**
   * Set loading state (useful for async confirmations)
   */
  function setLoading(value: boolean) {
    loading.value = value
  }

  /**
   * Props to bind to ConfirmDialog component
   */
  const ConfirmDialogProps = computed(() => ({
    'modelValue': isOpen.value,
    'onUpdate:modelValue': (value?: boolean) => {
      isOpen.value = value ?? false
      if (!value) {
        handleCancel()
      }
    },
    'title': options.value.title,
    'message': options.value.message,
    'confirmLabel': options.value.confirmLabel,
    'cancelLabel': options.value.cancelLabel,
    'confirmColor': options.value.confirmColor,
    'icon': options.value.icon,
    'loading': loading.value
  }))

  return {
    confirm,
    handleConfirm,
    handleCancel,
    setLoading,
    isOpen: readonly(isOpen),
    loading: readonly(loading),
    ConfirmDialogProps
  }
}
