import { defineStore } from 'pinia'

interface CartItem {
  itemId: string
  scheduleId: string
  tourId: string
  tourName: string
  tourSlug?: string
  startDatetime?: string
  numParticipants: number
  pricePerParticipant: number
  itemTotal: number
  durationHours?: number
}

interface Cart {
  cartId: string | null
  items: CartItem[]
  cartTotal: number
}

export const useCartStore = defineStore('cart', () => {
  // El backend es la ÚNICA fuente de verdad
  const cart = ref<Cart>({
    cartId: null,
    items: [],
    cartTotal: 0
  })

  const isLoading = ref(false)
  const toast = useToast()

  const totalItems = computed(() =>
    cart.value.items.reduce((sum, item) => sum + item.numParticipants, 0)
  )

  const totalPrice = computed(() => cart.value.cartTotal)

  /**
   * Fetch cart from backend
   * Backend handles both authenticated users and anonymous users via cartId cookie
   */
  async function fetchCart() {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart', {
        credentials: 'include'
      })
      cart.value = response
    } catch (error: any) {
      console.error('Error fetching cart:', error)
      // On error, reset to empty cart
      clearCart()
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Add item to cart
   * Always calls backend API - no local logic
   */
  async function addItem(itemData: any) {
    isLoading.value = true
    try {
      const body = typeof itemData.scheduleId === 'string'
        ? { scheduleId: itemData.scheduleId, numParticipants: itemData.numParticipants }
        : { ...itemData }

      const response = await $fetch<Cart>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        body
      })
      cart.value = response

      toast.add({ title: 'Agregado al carrito', color: 'success' })
      return true
    } catch (error: any) {
      console.error('Error adding item to cart:', error)
      toast.add({
        title: 'Error',
        description: error.data?.message || 'Failed to add item to cart',
        color: 'error'
      })
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Remove item from cart
   * Always calls backend API - no local logic
   */
  async function removeItem(itemId: string) {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      cart.value = response

      toast.add({
        title: 'Eliminado',
        description: 'Item eliminado del carrito',
        color: 'success'
      })

      return true
    } catch (error: any) {
      console.error('Error removing item from cart:', error)
      toast.add({
        title: 'Error',
        description: error.data?.message || 'No se pudo eliminar el item',
        color: 'error'
      })
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Clear cart (useful after successful checkout)
   */
  function clearCart() {
    cart.value = {
      cartId: null,
      items: [],
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
    clearCart,
  }
})
