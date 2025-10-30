import { defineStore } from 'pinia'
import type { TourRes } from '~/lib/api-client' // Tipos del cliente generado
import type { TourScheduleRes } from '~/lib/api-client' // Necesitarás este tipo también

interface CartItem {
  schedule: TourScheduleRes;
  date: string;
  participants: number;
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalItems = computed(() => items.value.reduce((sum, item) => sum + item.participants, 0))

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const price = item.schedule.tour.price || 0
      return total + (item.participants * price)
    }, 0)
  })

  function addItem(item: CartItem) {
    const existingItem = items.value.find(i => i.schedule.id === item.schedule.id)
    if (existingItem) {
      existingItem.participants += item.participants
    } else {
      items.value.push(item)
    }
  }

  function removeItem(scheduleId: string) {
    items.value = items.value.filter(i => i.schedule.id !== scheduleId)
  }

  return { items, totalItems, totalPrice, addItem, removeItem }
})
