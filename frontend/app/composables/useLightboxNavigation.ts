/**
 * Composable for lightbox/gallery navigation with keyboard support.
 *
 * @example
 * const { currentIndex, currentItem, goToPrevious, goToNext, canGoNext, canGoPrevious }
 *   = useLightboxNavigation(images, toRef(() => props.initialIndex), {
 *       loop: true,
 *       onClose: () => emit('update:modelValue', false)
 *     })
 */
export function useLightboxNavigation<T>(
  items: Ref<T[]>,
  initialIndex: Ref<number>,
  options?: {
    loop?: boolean
    onClose?: () => void
    onReachEnd?: () => void
  }
) {
  const currentIndex = ref(initialIndex.value)

  const currentItem = computed(() => items.value[currentIndex.value])

  const canGoPrevious = computed(() => {
    if (options?.loop) return items.value.length > 1
    return currentIndex.value > 0
  })

  const canGoNext = computed(() => {
    if (options?.loop) return items.value.length > 1
    return currentIndex.value < items.value.length - 1
  })

  function goToPrevious() {
    if (currentIndex.value > 0) {
      currentIndex.value--
    } else if (options?.loop && items.value.length > 1) {
      currentIndex.value = items.value.length - 1
    }
  }

  function goToNext() {
    if (currentIndex.value < items.value.length - 1) {
      currentIndex.value++
    } else if (options?.loop && items.value.length > 1) {
      currentIndex.value = 0
    } else if (options?.onReachEnd) {
      options.onReachEnd()
    }
  }

  function goToIndex(index: number) {
    if (index >= 0 && index < items.value.length) {
      currentIndex.value = index
    }
  }

  function handleKeydown(event: KeyboardEvent) {
    switch (event.key) {
      case 'ArrowLeft':
        goToPrevious()
        break
      case 'ArrowRight':
        goToNext()
        break
      case 'Escape':
        options?.onClose?.()
        break
    }
  }

  // Watch for initial index changes from parent
  watch(initialIndex, (newIndex) => {
    currentIndex.value = newIndex
  })

  // Setup keyboard listeners
  onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
  })

  return {
    currentIndex: readonly(currentIndex),
    currentItem,
    canGoPrevious,
    canGoNext,
    goToPrevious,
    goToNext,
    goToIndex,
    totalItems: computed(() => items.value.length)
  }
}
