import { defineStore } from 'pinia'
import type { CartRes, CartItemReq, CartItemRes } from 'api-client'
import logger from '~/utils/logger'

declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void
  }
}

export const useCartStore = defineStore('cart', () => {
  const { showErrorToast, showSuccessToast, isScheduleFullError, parseError } = useApiError()
  const { t } = useI18n()

  const defaultCart: CartRes = {
    cartId: '',
    items: [],
    subtotal: 0,
    taxAmount: 0,
    taxRate: 0.19,
    cartTotal: 0
  }

  // === STATE ===

  // Internal cart state - always initialized with default
  const _cart = ref<CartRes>({ ...defaultCart })

  // Loading state for UI feedback
  const isLoading = ref(false)

  // Track if cart has been fetched from backend (prevents stale state after redirects)
  const isInitialized = ref(false)

  // Track pending operations to prevent duplicate requests (key = scheduleId or 'fetch')
  const pendingOperations = ref<Set<string>>(new Set())

  // === COMPUTED (Safe getters) ===

  // Safe cart getter - always returns a valid cart object (never undefined)
  const cart = computed(() => {
    return _cart.value ?? defaultCart
  })

  // Safe items getter for direct access
  const items = computed(() => {
    return _cart.value?.items ?? []
  })

  const totalItems = computed(() => {
    return _cart.value?.items?.reduce((sum, item) => sum + item.numParticipants, 0) ?? 0
  })

  const totalPrice = computed(() => {
    return _cart.value?.cartTotal ?? 0
  })

  const cartTotal = computed(() => {
    return _cart.value?.cartTotal ?? 0
  })

  // Check if an operation is in progress for a specific key
  const isOperationPending = (key: string) => pendingOperations.value.has(key)

  // === HELPERS ===

  function startOperation(key: string): boolean {
    if (pendingOperations.value.has(key)) {
      logger.warn(`Operation already pending for: ${key}`)
      return false // Operation already in progress
    }
    pendingOperations.value.add(key)
    return true
  }

  function endOperation(key: string) {
    pendingOperations.value.delete(key)
  }

  // === ACTIONS ===

  /**
   * Fetch cart from backend.
   * Prevents duplicate fetches using pendingOperations.
   */
  async function fetchCart(): Promise<void> {
    const operationKey = 'fetch'

    // Prevent duplicate fetch
    if (!startOperation(operationKey)) {
      return
    }

    isLoading.value = true
    try {
      const response = await $fetch<CartRes>('/api/cart', {
        credentials: 'include'
      })
      _cart.value = response
      isInitialized.value = true
    } catch (error) {
      logger.error('Error fetching cart:', error)
      // Reset to default on error
      _cart.value = { ...defaultCart }
      isInitialized.value = true
    } finally {
      isLoading.value = false
      endOperation(operationKey)
    }
  }

  /**
   * Add item to cart with duplicate-click protection.
   * Uses optimistic UI update for fast feedback.
   */
  async function addItem(itemData: CartItemReq): Promise<boolean> {
    const operationKey = `add:${itemData.scheduleId}`

    // Prevent duplicate add for same schedule
    if (!startOperation(operationKey)) {
      logger.warn('Add operation already in progress for this schedule')
      return false
    }

    const previousCart = JSON.parse(JSON.stringify(_cart.value)) as CartRes

    // Optimistic update: add item immediately for fast UI feedback
    const optimisticItem = {
      scheduleId: itemData.scheduleId,
      numParticipants: itemData.numParticipants,
      itemTotal: 0,
      pricePerParticipant: 0
    } as unknown as CartItemRes

    // Ensure items array exists
    if (!_cart.value.items) {
      _cart.value.items = []
    }

    const existingIndex = _cart.value.items.findIndex(
      item => item.scheduleId === itemData.scheduleId
    )

    if (existingIndex >= 0) {
      const existing = _cart.value.items[existingIndex]
      if (existing) {
        existing.numParticipants = existing.numParticipants + itemData.numParticipants
      }
    } else {
      _cart.value.items.push(optimisticItem)
    }

    isLoading.value = true
    try {
      const response = await $fetch<CartRes>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        body: itemData
      })
      _cart.value = response

      // Google Analytics tracking
      if (typeof window !== 'undefined' && window.gtag) {
        const addedItem = response.items.find(item => item.scheduleId === itemData.scheduleId)
        if (addedItem) {
          window.gtag('event', 'add_to_cart', {
            currency: 'CLP',
            value: addedItem.itemTotal,
            items: [{
              item_id: addedItem.scheduleId,
              item_name: addedItem.tourName,
              price: addedItem.pricePerParticipant,
              quantity: addedItem.numParticipants
            }]
          })
        }
      }

      return true
    } catch (error) {
      logger.error('Error adding item to cart:', error)
      // Revert to previous state on error
      _cart.value = previousCart

      if (isScheduleFullError(error)) {
        const apiError = parseError(error)
        showErrorToast(error, t('errors.business.schedule_full', { availableSlots: apiError.availableSlots ?? 0 }))
      } else {
        showErrorToast(error, t('cart.error_adding', 'Error al agregar al carrito'))
      }
      return false
    } finally {
      isLoading.value = false
      endOperation(operationKey)
    }
  }

  /**
   * Remove item from cart with duplicate-click protection.
   */
  async function removeItem(itemId: string): Promise<boolean> {
    const operationKey = `remove:${itemId}`

    // Prevent duplicate remove
    if (!startOperation(operationKey)) {
      logger.warn('Remove operation already in progress for this item')
      return false
    }

    const previousCart = JSON.parse(JSON.stringify(_cart.value)) as CartRes

    // Optimistic update: remove immediately
    if (_cart.value.items) {
      _cart.value.items = _cart.value.items.filter(
        item => item.itemId !== itemId && item.scheduleId !== itemId
      )
    }

    isLoading.value = true
    try {
      const response = await $fetch<CartRes>(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      _cart.value = response

      showSuccessToast(
        t('cart.removed', 'Eliminado'),
        t('cart.item_removed', 'Item eliminado del carrito')
      )

      return true
    } catch (error) {
      logger.error('Error removing item from cart:', error)
      // Revert on error
      _cart.value = previousCart
      showErrorToast(error, t('cart.error_removing', 'Error al eliminar del carrito'))
      return false
    } finally {
      isLoading.value = false
      endOperation(operationKey)
    }
  }

  /**
   * Clear cart completely (local + backend).
   * Called after successful payment.
   */
  async function clearCart(): Promise<void> {
    // Clear local state immediately
    _cart.value = { ...defaultCart }
    pendingOperations.value.clear()
    isInitialized.value = false

    // Clear on backend (fire and forget)
    try {
      await $fetch('/api/cart', {
        method: 'DELETE',
        credentials: 'include'
      })
    } catch (error) {
      logger.warn('Could not clear cart on backend:', error)
    }
  }

  /**
   * Reset cart state completely.
   * Use after payment redirect returns to ensure fresh state.
   */
  function resetState(): void {
    _cart.value = { ...defaultCart }
    pendingOperations.value.clear()
    isInitialized.value = false
    isLoading.value = false
  }

  /**
   * Ensure cart is loaded. Call this on pages that need cart data.
   * Safe to call multiple times - will only fetch once.
   */
  async function ensureLoaded(): Promise<void> {
    if (!isInitialized.value && !isOperationPending('fetch')) {
      await fetchCart()
    }
  }

  // === CART CONFLICT HANDLING (for login during checkout) ===

  // SessionStorage key for pre-login cart persistence
  const PRE_LOGIN_CART_KEY = 'preLoginCart'

  // Store the pre-login cart for conflict detection
  const preLoginCart = ref<CartItemRes[]>([])

  // Restore pre-login cart from sessionStorage on initialization
  if (import.meta.client) {
    try {
      const stored = sessionStorage.getItem(PRE_LOGIN_CART_KEY)
      if (stored) {
        preLoginCart.value = JSON.parse(stored)
      }
    } catch {
      // Ignore parsing errors
    }
  }

  /**
   * Save current cart state before login attempt.
   * Called before authentication to preserve local cart.
   * Persists to sessionStorage to survive page refreshes.
   */
  function savePreLoginCart(): void {
    const items = JSON.parse(JSON.stringify(_cart.value.items || []))
    preLoginCart.value = items
    if (import.meta.client) {
      try {
        sessionStorage.setItem(PRE_LOGIN_CART_KEY, JSON.stringify(items))
      } catch {
        // Ignore storage errors
      }
    }
  }

  /**
   * Check if there's a cart conflict after login.
   * Returns true if the backend cart differs from pre-login cart.
   */
  function hasCartConflict(): boolean {
    const currentItems = _cart.value.items || []
    const savedItems = preLoginCart.value

    // No conflict if either cart is empty
    if (currentItems.length === 0 || savedItems.length === 0) {
      return false
    }

    // Check if the items are different
    if (currentItems.length !== savedItems.length) {
      return true
    }

    // Check if schedules match
    const currentScheduleIds = new Set(currentItems.map(i => i.scheduleId))
    const savedScheduleIds = new Set(savedItems.map(i => i.scheduleId))

    for (const id of savedScheduleIds) {
      if (!currentScheduleIds.has(id)) {
        return true
      }
    }

    // Check if participant counts match
    for (const saved of savedItems) {
      const current = currentItems.find(i => i.scheduleId === saved.scheduleId)
      if (!current || current.numParticipants !== saved.numParticipants) {
        return true
      }
    }

    return false
  }

  /**
   * Get the pre-login cart items (for displaying in conflict modal).
   */
  function getPreLoginCart(): CartItemRes[] {
    return preLoginCart.value
  }

  /**
   * Resolve cart conflict based on user choice.
   * @param choice - 'current' (keep backend cart), 'saved' (use pre-login cart), or 'merge'
   */
  /**
   * Clear the pre-login cart from memory and sessionStorage.
   */
  function clearPreLoginCart(): void {
    preLoginCart.value = []
    if (import.meta.client) {
      try {
        sessionStorage.removeItem(PRE_LOGIN_CART_KEY)
      } catch {
        // Ignore storage errors
      }
    }
  }

  async function resolveCartConflict(choice: 'current' | 'saved' | 'merge'): Promise<void> {
    const savedItems = preLoginCart.value

    if (choice === 'current') {
      // Keep current backend cart - nothing to do
      clearPreLoginCart()
      return
    }

    if (choice === 'saved') {
      // Replace with pre-login cart
      // Clear current cart and add saved items
      await clearCart()
      for (const item of savedItems) {
        await addItem({
          scheduleId: item.scheduleId,
          numParticipants: item.numParticipants
        })
      }
      clearPreLoginCart()
      return
    }

    if (choice === 'merge') {
      // Merge: add pre-login items that aren't already in current cart
      const currentScheduleIds = new Set((_cart.value.items || []).map(i => i.scheduleId))

      for (const item of savedItems) {
        if (!currentScheduleIds.has(item.scheduleId)) {
          // Item not in current cart, add it
          await addItem({
            scheduleId: item.scheduleId,
            numParticipants: item.numParticipants
          })
        }
        // If item already exists, we could optionally increase participant count
        // For now, we skip duplicates to avoid overbooking issues
      }
      clearPreLoginCart()
    }
  }

  return {
    // Safe getters (computed, always return valid values)
    cart,
    items,
    totalItems,
    totalPrice,
    cartTotal,
    // State
    isLoading,
    isInitialized,
    // Actions
    fetchCart,
    addItem,
    removeItem,
    clearCart,
    resetState,
    ensureLoaded,
    // Cart conflict handling
    savePreLoginCart,
    hasCartConflict,
    getPreLoginCart,
    resolveCartConflict,
    // Utility (for debugging/UI)
    isOperationPending
  }
})
