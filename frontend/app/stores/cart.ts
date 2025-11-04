import { defineStore } from 'pinia'
import { useLocalStorage } from '@vueuse/core' // 👈 PASO 1: Importar
import { useAuthStore } from './auth'

// Las interfaces CartItem y Cart se mantienen igual
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
  // 👇 PASO 2: Reemplazar ref() con useLocalStorage
  // El primer argumento es la clave en localStorage.
  // El segundo es el valor inicial si no se encuentra nada.
  const cart = useLocalStorage<Cart>('anonymous_cart', {
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

  // 🗑️ PASO 3: Eliminar `loadLocalCart` y `saveLocalCart`. ¡Ya no son necesarios!

  // Esta función ahora es para el flujo de usuario autenticado
  async function fetchCart() {
    isLoading.value = true
    try {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        // Si no está autenticado, el carrito ya está cargado desde localStorage. No hacer nada.
        return
      }
      
      const response = await $fetch<Cart>('/api/cart')
      cart.value = response // Actualiza el estado local con la respuesta del backend
    } catch (error: any) {
      console.error('Error fetching cart:', error)
      // En caso de error, podríamos volver al carrito local o simplemente limpiarlo.
      clearCart()
    } finally {
      isLoading.value = false
    }
  }

  // La lógica para añadir un item se simplifica
  async function addItem(itemData: any) {
    const authStore = useAuthStore()
    if (authStore.isAuthenticated) {
      // Lógica para usuario autenticado (sin cambios)
      // ... llamada a /api/cart/items ...
      // La respuesta del backend actualizará `cart.value`
      isLoading.value = true
      try {
        const body = typeof itemData.scheduleId === 'string'
          ? { scheduleId: itemData.scheduleId, numParticipants: itemData.numParticipants }
          : { ...itemData }

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
    } else {
      // Lógica para usuario anónimo (ahora simplificada)
      const newItem: CartItem = {
        itemId: `local-${Date.now()}`,
        scheduleId: itemData.scheduleId,
        tourId: itemData.tourId,
        tourName: itemData.tourName,
        tourSlug: itemData.tourSlug,
        startDatetime: itemData.startDatetime,
        numParticipants: itemData.numParticipants,
        pricePerParticipant: itemData.pricePerParticipant,
        itemTotal: (itemData.pricePerParticipant || 0) * (itemData.numParticipants || 1),
        durationHours: itemData.durationHours
      }
      cart.value.items.push(newItem)
      cart.value.cartTotal = cart.value.items.reduce((sum, item) => sum + item.itemTotal, 0)
      // 🚀 ¡No se necesita saveLocalCart()! useLocalStorage lo hace automáticamente.
      toast.add({ title: 'Agregado al carrito', color: 'success' })
      return true
    }
  }
  
  // La lógica para eliminar un item también se simplifica
  async function removeItem(itemId: string) {
    const authStore = useAuthStore()
    if (authStore.isAuthenticated && !itemId.startsWith('local-')) {
       // Lógica para usuario autenticado (sin cambios)
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
    } else {
      // Lógica para usuario anónimo (ahora simplificada)
      cart.value.items = cart.value.items.filter(item => item.itemId !== itemId)
      cart.value.cartTotal = cart.value.items.reduce((sum, item) => sum + item.itemTotal, 0)
      // 🚀 ¡No se necesita saveLocalCart()! useLocalStorage lo hace automáticamente.
      toast.add({
        title: 'Eliminado',
        description: 'Item eliminado del carrito',
        color: 'success'
      })
      return true
    }
  }

  // Clear cart (useful after successful checkout)
  function clearCart() {
    cart.value = {
      cartId: null,
      items: [],
      cartTotal: 0
    }
    // 🚀 ¡No se necesita localStorage.removeItem()! useLocalStorage lo hace automáticamente.
  }

  // 🗑️ PASO 4: Eliminar `loadLocalCart()` de la inicialización. ¡Ya no es necesario!

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