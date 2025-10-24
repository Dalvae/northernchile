import { defineStore } from 'pinia'
import type { TourRes } from '~/lib/api-client' // Tipos del cliente generado

interface CartItem {
  tour: TourRes;
  date: string;
  adults: number;
  children: number;
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalItems = computed(() => items.value.reduce((sum, item) => sum + item.adults + item.children, 0))

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const adultPrice = item.tour.priceAdult || 0
      const childPrice = item.tour.priceChild || 0
      return total + (item.adults * adultPrice) + (item.children * childPrice)
    }, 0)
  })

  function addItem(item: CartItem) {
    // Lógica para añadir o actualizar un item
    const existingItem = items.value.find(i => i.tour.id === item.tour.id && i.date === item.date)
    if (existingItem) {
      existingItem.adults += item.adults
      existingItem.children += item.children
    } else {
      items.value.push(item)
    }
  }

  function removeItem(tourId: string, date: string) {
    items.value = items.value.filter(i => !(i.tour.id === tourId && i.date === date))
  }

  return { items, totalItems, totalPrice, addItem, removeItem }
})
