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

  // Load cart from localStorage on initialization
  function loadLocalCart() {
    if (process.client) {
      const stored = localStorage.getItem('anonymous_cart')
      if (stored) {
        try {
          cart.value = JSON.parse(stored)
        } catch (e) {
          console.error('Failed to parse local cart:', e)
        }
      }
    }
  }

  // Save cart to localStorage
  function saveLocalCart() {
    if (process.client) {
      localStorage.setItem('anonymous_cart', JSON.stringify(cart.value))
    }
  }

  // Fetch cart from backend
  async function fetchCart() {
    isLoading.value = true
    try {
      // Get auth token
      const authStore = useAuthStore()
      const headers: Record<string, string> = {}
      if (authStore.token) {
        headers['Authorization'] = `Bearer ${authStore.token}`
      }

      const response = await $fetch<Cart>('/api/cart', {
        credentials: 'include',
        headers
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
  async function addItem(scheduleIdOrItem: string | any, numParticipants: number = 1) {
    const authStore = useAuthStore()

    console.log('Cart store - isAuthenticated:', authStore.isAuthenticated)
    console.log('Cart store - token exists:', !!authStore.token)

    // For now, always use local cart (simpler for MVP)
    // In checkout, we'll migrate local cart to backend
    return addItemToLocal(scheduleIdOrItem, numParticipants)
  }

  // Add item to backend cart (authenticated users)
  async function addItemToBackend(scheduleIdOrItem: string | any, numParticipants: number = 1) {
    isLoading.value = true
    try {
      const body = typeof scheduleIdOrItem === 'string'
        ? { scheduleId: scheduleIdOrItem, numParticipants }
        : {
            scheduleId: scheduleIdOrItem.tourScheduleId,
            numParticipants: scheduleIdOrItem.participantCount || numParticipants
          }

      const authStore = useAuthStore()
      const headers: Record<string, string> = {}
      if (authStore.token) {
        headers['Authorization'] = `Bearer ${authStore.token}`
      }

      const response = await $fetch<Cart>('/api/cart/items', {
        method: 'POST',
        credentials: 'include',
        headers,
        body
      })
      cart.value = response

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

  // Add item to local cart (anonymous users)
  function addItemToLocal(scheduleIdOrItem: any, numParticipants: number = 1) {
    try {
      const itemData = typeof scheduleIdOrItem === 'string'
        ? { scheduleId: scheduleIdOrItem, numParticipants }
        : scheduleIdOrItem

      const newItem: CartItem = {
        itemId: `local-${Date.now()}-${Math.random()}`,
        scheduleId: itemData.tourScheduleId || itemData.scheduleId,
        tourId: itemData.tourId,
        tourName: itemData.tourName,
        tourSlug: itemData.tourSlug,
        startDatetime: itemData.startDatetime,
        numParticipants: itemData.participantCount || numParticipants,
        pricePerParticipant: itemData.price,
        itemTotal: (itemData.price || 0) * (itemData.participantCount || numParticipants),
        durationHours: itemData.durationHours
      }

      cart.value.items.push(newItem)
      cart.value.cartTotal = cart.value.items.reduce((sum, item) => sum + item.itemTotal, 0)

      saveLocalCart()
      return true
    } catch (error) {
      console.error('Error adding item to local cart:', error)
      toast.add({
        title: 'Error',
        description: 'No se pudo agregar al carrito',
        color: 'error'
      })
      return false
    }
  }

  // Remove item from cart
  async function removeItem(itemId: string) {
    const authStore = useAuthStore()

    // If local cart item (starts with 'local-')
    if (itemId.startsWith('local-')) {
      cart.value.items = cart.value.items.filter(item => item.itemId !== itemId)
      cart.value.cartTotal = cart.value.items.reduce((sum, item) => sum + item.itemTotal, 0)
      saveLocalCart()

      toast.add({
        title: 'Eliminado',
        description: 'Item eliminado del carrito',
        color: 'success'
      })
      return true
    }

    // Backend cart item
    isLoading.value = true
    try {
      const headers: Record<string, string> = {}
      if (authStore.token) {
        headers['Authorization'] = `Bearer ${authStore.token}`
      }

      const response = await $fetch<Cart>(`/api/cart/items/${itemId}`, {
        method: 'DELETE',
        credentials: 'include',
        headers
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

  // Clear cart (useful after successful checkout)
  function clearCart() {
    cart.value = {
      cartId: null,
      items: [],
      cartTotal: 0
    }
    if (process.client) {
      localStorage.removeItem('anonymous_cart')
    }
  }

  // Initialize cart from localStorage
  loadLocalCart()

  return {
    cart,
    isLoading,
    totalItems,
    totalPrice,
    fetchCart,
    addItem,
    removeItem,
    clearCart,
    loadLocalCart
  }
})
