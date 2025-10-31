import { defineStore } from 'pinia'

interface CartItem {
  itemId: string
  scheduleId: string
  tourId: string
  tourName: string
  numParticipants: number
  pricePerParticipant: number
  itemTotal: number
}

interface Cart {
  cartId: string | null
  items: CartItem[]
  cartTotal: number
}

export const useCartStore = defineStore('cart', () => {
  const cart = ref<Cart>({
    cartId: null,
    items: [],
    cartTotal: 0
  })

  const isLoading = ref(false)
  const toast = useToast()

  // Computed properties
  const totalItems = computed(() =>
    cart.value.items.reduce((sum, item) => sum + item.numParticipants, 0)
  )

  const totalPrice = computed(() => cart.value.cartTotal)

  // Fetch cart from backend
  async function fetchCart() {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart', {
        credentials: 'include'
      })
      cart.value = response
    } catch (error: any) {
      console.error('Error fetching cart:', error)
      // Initialize empty cart on error
      cart.value = {
        cartId: null,
        items: [],
        cartTotal: 0
      }
    } finally {
      isLoading.value = false
    }
  }

  // Add item to cart
  async function addItem(scheduleId: string, numParticipants: number = 1) {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        body: {
          scheduleId,
          numParticipants
        }
      })
      cart.value = response

      toast.add({
        title: 'Success',
        description: 'Item added to cart',
        color: 'success'
      })

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

  // Remove item from cart
  async function removeItem(itemId: string) {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      cart.value = response

      toast.add({
        title: 'Success',
        description: 'Item removed from cart',
        color: 'success'
      })

      return true
    } catch (error: any) {
      console.error('Error removing item from cart:', error)
      toast.add({
        title: 'Error',
        description: error.data?.message || 'Failed to remove item from cart',
        color: 'error'
      })
      return false
    } finally {
      isLoading.value = false
    }
  }

  // Clear cart (useful after successful checkout)
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
    clearCart
  }
})
