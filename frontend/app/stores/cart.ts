import { defineStore } from 'pinia'

interface AddCartItemRequest {
  scheduleId: string
  numParticipants: number
}

interface CartItem {
  id?: string
  scheduleId: string
  numParticipants: number
  tourName?: string
  pricePerParticipant?: number
  itemTotal?: number
  startDatetime?: string
  durationHours?: number
}

interface Cart {
  cartId: string | null
  items: CartItem[]
  subtotal: number
  taxAmount: number
  taxRate: number
  cartTotal: number
}

declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void
  }
}

export const useCartStore = defineStore('cart', () => {
  const { showErrorToast, showSuccessToast, isScheduleFullError, parseError } = useApiError()
  const { t } = useI18n()

  const defaultCart: Cart = {
    cartId: null,
    items: [],
    subtotal: 0,
    taxAmount: 0,
    taxRate: 0.19,
    cartTotal: 0
  }

  const cart = ref<Cart>(defaultCart)

  const isLoading = ref(false)

  const totalItems = computed(() => {
    if (!cart.value || !cart.value.items) return 0
    return cart.value.items.reduce((sum, item) => sum + item.numParticipants, 0)
  })

  const totalPrice = computed(() => {
    if (!cart.value) return 0
    return cart.value.cartTotal || 0
  })

  async function fetchCart() {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart', {
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
  async function addItem(itemData: AddCartItemRequest) {
    const previousCart = JSON.parse(JSON.stringify(cart.value)) as Cart

    // Optimistic update: add item immediately
    const optimisticItem: CartItem = {
      scheduleId: itemData.scheduleId,
      numParticipants: itemData.numParticipants
    }

    const existingIndex = cart.value.items.findIndex(
      item => item.scheduleId === itemData.scheduleId
    )

    if (existingIndex >= 0) {
      const existing = cart.value.items[existingIndex]
      if (existing) {
        existing.numParticipants += itemData.numParticipants
      }
    } else {
      cart.value.items.push(optimisticItem)
    }

    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart/items', {
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
    const previousCart = JSON.parse(JSON.stringify(cart.value)) as Cart

    // Optimistic update: remove item immediately
    cart.value.items = cart.value.items.filter(item => item.id !== itemId && item.scheduleId !== itemId)

    isLoading.value = true
    try {
      const response = await $fetch<Cart>(`/api/cart/items/${itemId}`, {
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

  function clearCart() {
    cart.value = {
      cartId: null,
      items: [],
      subtotal: 0,
      taxAmount: 0,
      taxRate: 0.19,
      cartTotal: 0
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
