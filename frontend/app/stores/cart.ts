import { defineStore } from 'pinia'
import type { TourRes } from '~/lib/api-client' // Tipos del cliente generado
import type { TourScheduleRes } from '~/lib/api-client' // Necesitarás este tipo también

interface CartItem {
  // Ahora guardamos la instancia del horario, que contiene la información del tour.
  schedule: TourScheduleRes; 
  date: string;
  adults: number;
  children: number;
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalItems = computed(() => items.value.reduce((sum, item) => sum + item.adults + item.children, 0))

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const adultPrice = item.schedule.tour.priceAdult || 0 // Acceder a través de schedule.tour
      const childPrice = item.schedule.tour.priceChild || 0
      return total + (item.adults * adultPrice) + (item.children * childPrice)
    }, 0)
  })

  function addItem(item: CartItem) {
    // Lógica para añadir o actualizar un item
    const existingItem = items.value.find(i => i.schedule.id === item.schedule.id)
    if (existingItem) {
      existingItem.adults += item.adults
      existingItem.children += item.children
    } else {
      items.value.push(item)
    }
  }

  function removeItem(scheduleId: string) {
    items.value = items.value.filter(i => i.schedule.id !== scheduleId)
  }

  return { items, totalItems, totalPrice, addItem, removeItem }
})
