import { defineStore } from 'pinia'

interface CartItem {
  scheduleId: string
  numParticipants: number
}

interface Cart {
  cartId: string | null
  items: CartItem[]
  cartTotal: number
}

export const useCartStore = defineStore('cart', () => {
  const defaultCart: Cart = {
    cartId: null,
    items: [],
    cartTotal: 0
  }

  const cart = ref<Cart>(defaultCart)

  const isLoading = ref(false)
  const toast = useToast()

  const totalItems = computed(() => {
    // Si cart.value es null o undefined, o si no tiene items, devuelve 0
    if (!cart.value || !cart.value.items) return 0

    return cart.value.items.reduce((sum, item) => sum + item.numParticipants, 0)
  })

  const totalPrice = computed(() => {
    if (!cart.value) return 0
    return cart.value.cartTotal || 0
  })

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
    } catch (error) {
      console.error('Error fetching cart:', error)
      clearCart()
    } finally {
      isLoading.value = false
    }
  }

  async function addItem(itemData: AddCartItemRequest) {
    isLoading.value = true
    try {
      const response = await $fetch<Cart>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        body: itemData
      })
      cart.value = response

      // Google Analytics: Track add_to_cart event
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

      toast.add({ title: 'Agregado al carrito', color: 'success' })
      return true
    } catch (error) {
      console.error('Error adding item to cart:', error)
      const errorMessage = error && typeof error === 'object' && 'data' in error
        ? (error.data as { message?: string })?.message
        : undefined
      toast.add({
        title: 'Error',
        description: errorMessage || 'Failed to add item to cart',
        color: 'error'
      })
      return false
    } finally {
      isLoading.value = false
    }
  }

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
    } catch (error) {
      console.error('Error removing item from cart:', error)
      const errorMessage = error && typeof error === 'object' && 'data' in error
        ? (error.data as { message?: string })?.message
        : undefined
      toast.add({
        title: 'Error',
        description: errorMessage || 'No se pudo eliminar el item',
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

  onMounted(() => {
    if (import.meta.client) {
      try {
        const stored = localStorage.getItem('nc-cart')
        if (stored) {
          cart.value = JSON.parse(stored)
        }
      } catch (e) {
        console.error('Error leyendo carrito local', e)
      }
    }
  })

  watch(cart, (newVal) => {
    if (import.meta.client) {
      localStorage.setItem('nc-cart', JSON.stringify(newVal))
    }
  }, { deep: true })

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
