import { defineStore } from 'pinia'
import type { CartRes, CartItemReq, CartItemRes } from 'api-client'

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

  const cart = ref<CartRes>(defaultCart)

  const isLoading = ref(false)

  // Safe accessors with optional chaining to prevent errors during hydration
  const totalItems = computed(() => {
    return cart.value?.items?.reduce((sum, item) => sum + item.numParticipants, 0) ?? 0
  })

  const totalPrice = computed(() => {
    return cart.value?.cartTotal ?? 0
  })

  async function fetchCart() {
    isLoading.value = true
    try {
      const response = await $fetch<CartRes>('/api/cart', {
        credentials: 'include'
      })
      cart.value = response
    } catch (error) {
      console.error('Error fetching cart:', error)
      clearCart()
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Add item to cart with optimistic UI update.
   * Updates UI immediately, then syncs with backend. Reverts on failure.
   */
  async function addItem(itemData: CartItemReq) {
    const previousCart = JSON.parse(JSON.stringify(cart.value)) as CartRes

    // Optimistic update: add item immediately
    // We cast to CartItemRes because we don't have all fields yet
    const optimisticItem = {
      scheduleId: itemData.scheduleId,
      numParticipants: itemData.numParticipants,
      // Default values for other fields to satisfy UI
      itemTotal: 0,
      pricePerParticipant: 0
    } as unknown as CartItemRes

    const existingIndex = cart.value.items.findIndex(
      item => item.scheduleId === itemData.scheduleId
    )

    if (existingIndex >= 0) {
      const existing = cart.value.items[existingIndex]
      if (existing) {
        existing.numParticipants = existing.numParticipants + itemData.numParticipants
      }
    } else {
      cart.value.items.push(optimisticItem)
    }

    isLoading.value = true
    try {
      const response = await $fetch<CartRes>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        body: itemData
      })
      cart.value = response

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

      showSuccessToast(t('cart.added_to_cart', 'Agregado al carrito'))
      return true
    } catch (error) {
      console.error('Error adding item to cart:', error)
      // Revert to previous state
      cart.value = previousCart

      // Use useApiError to show appropriate message based on error code
      if (isScheduleFullError(error)) {
        const apiError = parseError(error)
        showErrorToast(error, t('errors.business.schedule_full', { availableSlots: apiError.availableSlots ?? 0 }))
      } else {
        showErrorToast(error, t('cart.error_adding', 'Error al agregar al carrito'))
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Remove item from cart with optimistic UI update.
   * Removes from UI immediately, then syncs with backend. Reverts on failure.
   */
  async function removeItem(itemId: string) {
    const previousCart = JSON.parse(JSON.stringify(cart.value)) as CartRes

    // Optimistic update: remove item immediately
    cart.value.items = cart.value.items.filter(item => item.itemId !== itemId && item.scheduleId !== itemId)

    isLoading.value = true
    try {
      const response = await $fetch<CartRes>(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      cart.value = response

      showSuccessToast(
        t('cart.removed', 'Eliminado'),
        t('cart.item_removed', 'Item eliminado del carrito')
      )

      return true
    } catch (error) {
      console.error('Error removing item from cart:', error)
      // Revert to previous state
      cart.value = previousCart
      showErrorToast(error, t('cart.error_removing', 'Error al eliminar del carrito'))
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Clear cart both locally and on the backend.
   * Called after successful payment to ensure cart is empty.
   */
  async function clearCart() {
    // Clear local state immediately
    cart.value = {
      cartId: '',
      items: [],
      subtotal: 0,
      taxAmount: 0,
      taxRate: 0.19,
      cartTotal: 0
    }

    // Also clear on the backend (fire and forget - don't block UI)
    try {
      await $fetch('/api/cart', {
        method: 'DELETE',
        credentials: 'include'
      })
    } catch (error) {
      // Silently ignore - cart is already cleared locally
      console.warn('Could not clear cart on backend:', error)
    }
  }

  return {
    cart,
    isLoading,
    totalItems,
    totalPrice,
    fetchCart,
    addItem,
    removeItem,
    clearCart
  }
})
