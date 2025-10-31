<script setup lang="ts">
const { t } = useI18n()
const toast = useToast()
const router = useRouter()
const config = useRuntimeConfig()
const authStore = useAuthStore()

useSeoMeta({
  title: 'Carrito - Northern Chile',
  description: 'Revisa tu carrito de compras y procede al pago de tus tours',
  robots: 'noindex, nofollow'
})

// Cart state
const { data: cartData, refresh: refreshCart, pending } = await useAsyncData(
  'cart',
  async () => {
    if (!authStore.isAuthenticated) {
      return null
    }
    try {
      const response = await $fetch<{
        id: string
        items: Array<{
          id: string
          tourSchedule: {
            id: string
            startDatetime: string
            tour: {
              id: string
              name: string
              duration: number
              basePrice: number
            }
          }
          quantity: number
          pricePerPerson: number
        }>
      }>(`${config.public.apiBase}/cart`, {
        credentials: 'include'
      })
      return response
    } catch (error) {
      return null
    }
  }
)

const cartItems = computed(() => cartData.value?.items || [])
const isEmpty = computed(() => cartItems.value.length === 0)

// Calculations
const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    return sum + (item.pricePerPerson * item.quantity)
  }, 0)
})

const tax = computed(() => subtotal.value * 0.19) // IVA 19%
const total = computed(() => subtotal.value + tax.value)

// Actions
async function removeItem(itemId: string) {
  try {
    await $fetch(`${config.public.apiBase}/cart/items/${itemId}`, {
      method: 'DELETE',
      credentials: 'include'
    })

    toast.add({
      title: 'Item eliminado',
      description: 'El tour ha sido removido de tu carrito',
      color: 'success'
    })

    await refreshCart()
  } catch (error) {
    toast.add({
      title: 'Error',
      description: 'No se pudo eliminar el item',
      color: 'error'
    })
  }
}

async function updateQuantity(itemId: string, quantity: number) {
  if (quantity < 1) return

  try {
    await $fetch(`${config.public.apiBase}/cart/items/${itemId}`, {
      method: 'PATCH',
      credentials: 'include',
      body: { quantity }
    })

    await refreshCart()
  } catch (error) {
    toast.add({
      title: 'Error',
      description: 'No se pudo actualizar la cantidad',
      color: 'error'
    })
  }
}

function proceedToCheckout() {
  if (isEmpty.value) return

  // Redirect to first tour booking page (in a real app, this would be a multi-item checkout)
  const firstItem = cartItems.value[0]
  router.push(`/tours/${firstItem.tourSchedule.tour.id}/book?scheduleId=${firstItem.tourSchedule.id}`)
}

// Format helpers
function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP'
  }).format(amount)
}

function formatDate(dateString: string) {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('es-CL', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-900 py-12">
    <UContainer>
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('cart.title') }}
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          {{ isEmpty ? t('cart.empty') : t('cart.items', { count: cartItems.length }) }}
        </p>
      </div>

      <!-- Not logged in -->
      <div v-if="!authStore.isAuthenticated" class="text-center py-16">
        <UIcon name="i-lucide-shopping-cart" class="w-20 h-20 text-neutral-300 dark:text-neutral-600 mx-auto mb-6" />
        <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white mb-4">
          Inicia sesión para ver tu carrito
        </h2>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          Necesitas iniciar sesión para acceder a tu carrito de compras
        </p>
        <UButton
          to="/auth"
          size="lg"
          color="primary"
          icon="i-lucide-log-in"
        >
          {{ t('nav.login') }}
        </UButton>
      </div>

      <!-- Empty cart -->
      <div v-else-if="isEmpty && !pending" class="text-center py-16">
        <UIcon name="i-lucide-shopping-cart" class="w-20 h-20 text-neutral-300 dark:text-neutral-600 mx-auto mb-6" />
        <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white mb-4">
          {{ t('cart.empty') }}
        </h2>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          No has agregado ningún tour a tu carrito aún
        </p>
        <UButton
          to="/tours"
          size="lg"
          color="primary"
          icon="i-lucide-telescope"
        >
          Explorar Tours
        </UButton>
      </div>

      <!-- Cart with items -->
      <div v-else-if="!isEmpty" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <UCard v-for="item in cartItems" :key="item.id">
            <div class="flex gap-6">
              <!-- Tour Info -->
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-neutral-900 dark:text-white mb-2">
                  {{ item.tourSchedule.tour.name }}
                </h3>
                <div class="space-y-1 text-sm text-neutral-600 dark:text-neutral-400">
                  <p class="flex items-center gap-2">
                    <UIcon name="i-lucide-calendar" class="w-4 h-4" />
                    {{ formatDate(item.tourSchedule.startDatetime) }}
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon name="i-lucide-clock" class="w-4 h-4" />
                    {{ item.tourSchedule.tour.duration }} horas
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon name="i-lucide-user" class="w-4 h-4" />
                    {{ formatCurrency(item.pricePerPerson) }} por persona
                  </p>
                </div>

                <!-- Quantity controls -->
                <div class="flex items-center gap-4 mt-4">
                  <span class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
                    Cantidad:
                  </span>
                  <div class="flex items-center gap-2">
                    <UButton
                      icon="i-lucide-minus"
                      size="sm"
                      color="neutral"
                      variant="soft"
                      square
                      @click="updateQuantity(item.id, item.quantity - 1)"
                      :disabled="item.quantity <= 1"
                    />
                    <span class="w-12 text-center font-semibold text-neutral-900 dark:text-white">
                      {{ item.quantity }}
                    </span>
                    <UButton
                      icon="i-lucide-plus"
                      size="sm"
                      color="neutral"
                      variant="soft"
                      square
                      @click="updateQuantity(item.id, item.quantity + 1)"
                    />
                  </div>
                </div>
              </div>

              <!-- Price & Remove -->
              <div class="flex flex-col items-end justify-between">
                <p class="text-xl font-bold text-neutral-900 dark:text-white">
                  {{ formatCurrency(item.pricePerPerson * item.quantity) }}
                </p>
                <UButton
                  icon="i-lucide-trash-2"
                  color="error"
                  variant="soft"
                  size="sm"
                  @click="removeItem(item.id)"
                >
                  {{ t('cart.remove') }}
                </UButton>
              </div>
            </div>
          </UCard>
        </div>

        <!-- Order Summary -->
        <div class="lg:col-span-1">
          <UCard>
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-6">
              Resumen del Pedido
            </h3>

            <div class="space-y-4 mb-6">
              <div class="flex justify-between text-neutral-600 dark:text-neutral-400">
                <span>{{ t('common.subtotal') }}</span>
                <span class="font-semibold">{{ formatCurrency(subtotal) }}</span>
              </div>
              <div class="flex justify-between text-neutral-600 dark:text-neutral-400">
                <span>{{ t('common.tax') }} (19%)</span>
                <span class="font-semibold">{{ formatCurrency(tax) }}</span>
              </div>
              <UDivider />
              <div class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white">
                <span>{{ t('common.total') }}</span>
                <span>{{ formatCurrency(total) }}</span>
              </div>
            </div>

            <UButton
              block
              size="lg"
              color="primary"
              icon="i-lucide-arrow-right"
              trailing
              @click="proceedToCheckout"
            >
              {{ t('cart.checkout') }}
            </UButton>

            <UButton
              block
              variant="soft"
              color="neutral"
              class="mt-3"
              icon="i-lucide-arrow-left"
              to="/tours"
            >
              {{ t('cart.continue_shopping') }}
            </UButton>

            <!-- Security badges -->
            <div class="mt-6 pt-6 border-t border-neutral-200 dark:border-neutral-700">
              <div class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400 mb-2">
                <UIcon name="i-lucide-shield-check" class="w-5 h-5 text-success-600" />
                <span>Pago seguro y encriptado</span>
              </div>
              <div class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400">
                <UIcon name="i-lucide-rotate-ccw" class="w-5 h-5 text-info-600" />
                <span>Cancelación gratis hasta 24h antes</span>
              </div>
            </div>
          </UCard>
        </div>
      </div>

      <!-- Loading state -->
      <div v-else-if="pending" class="text-center py-16">
        <UIcon name="i-lucide-loader-2" class="w-12 h-12 text-primary-600 animate-spin mx-auto mb-4" />
        <p class="text-neutral-600 dark:text-neutral-400">
          Cargando tu carrito...
        </p>
      </div>
    </UContainer>
  </div>
</template>
