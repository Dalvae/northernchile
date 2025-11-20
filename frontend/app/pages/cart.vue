<script setup lang="ts">
const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()
const { t, locale } = useI18n()
const localePath = useLocalePath()
const { formatPrice } = useCurrency()
const { formatDateTime } = useDateTime()

useSeoMeta({
  title: () => `${t('cart.title')} - Northern Chile`,
  description: () => t('cart.description'),
  robots: 'noindex, nofollow'
})

const isEmpty = computed(() => cartStore.cart.items.length === 0)

// Calculations
const subtotal = computed(() => cartStore.cart.cartTotal)
const tax = computed(() => subtotal.value * 0.19) // IVA 19%
const total = computed(() => subtotal.value + tax.value)

// Actions
async function removeItem(itemId: string) {
  await cartStore.removeItem(itemId)
}

function proceedToCheckout() {
  if (isEmpty.value) return
  router.push(localePath('/checkout'))
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-12">
    <UContainer>
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('cart.title') }}
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300">
          {{
            isEmpty
              ? t('cart.empty')
              : t('cart.items_count', { count: cartStore.totalItems }, cartStore.totalItems)
          }}
        </p>
      </div>

      <!-- Empty cart -->
      <div
        v-if="isEmpty"
        class="text-center py-16"
      >
        <UIcon
          name="i-lucide-shopping-cart"
          class="w-20 h-20 text-neutral-200 dark:text-neutral-600 mx-auto mb-6"
        />
        <h2
          class="text-2xl font-semibold text-neutral-900 dark:text-white mb-4"
        >
          {{ t('cart.empty') }}
        </h2>
        <p class="text-neutral-600 dark:text-neutral-300 mb-6">
          {{ t('cart.empty_description') }}
        </p>
        <UButton
          :to="localePath('/tours')"
          size="lg"
          color="primary"
          icon="i-lucide-telescope"
        >
          {{ t('cart.explore_tours') }}
        </UButton>
      </div>

      <!-- Cart with items -->
      <div
        v-else
        class="grid grid-cols-1 lg:grid-cols-3 gap-8"
      >
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <UCard
            v-for="item in cartStore.cart.items"
            :key="item.itemId"
          >
            <div class="flex gap-6">
              <!-- Tour Info -->
              <div class="flex-1">
                <h3
                  class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
                >
                  {{ item.tourName }}
                </h3>
                <div
                  class="space-y-1 text-sm text-neutral-600 dark:text-neutral-300"
                >
                  <p
                    v-if="item.startDatetime"
                    class="flex items-center gap-2"
                  >
                    <UIcon
                      name="i-lucide-calendar"
                      class="w-4 h-4"
                    />
                    {{ formatDateTime(item.startDatetime) }}
                  </p>
                  <p
                    v-if="item.durationHours"
                    class="flex items-center gap-2"
                  >
                    <UIcon
                      name="i-lucide-clock"
                      class="w-4 h-4"
                    />
                    {{ item.durationHours }} {{ t('cart.hours') }}
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-user"
                      class="w-4 h-4"
                    />
                    {{ formatPrice(item.pricePerParticipant) }} {{ t('cart.per_person') }}
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-users"
                      class="w-4 h-4"
                    />
                    {{ t('cart.items_count', { count: item.numParticipants }, item.numParticipants) }}
                  </p>
                </div>
              </div>

              <!-- Price & Remove -->
              <div class="flex flex-col items-end justify-between">
                <p class="text-xl font-bold text-neutral-900 dark:text-white">
                  {{ formatPrice(item.itemTotal) }}
                </p>
                <UButton
                  icon="i-lucide-trash-2"
                  color="error"
                  variant="soft"
                  size="sm"
                  @click="removeItem(item.itemId)"
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
            <h3
              class="text-xl font-semibold text-neutral-900 dark:text-white mb-6"
            >
              {{ t('cart.order_summary') }}
            </h3>

            <div class="space-y-4 mb-6">
              <div
                class="flex justify-between text-neutral-600 dark:text-neutral-300"
              >
                <span>{{ t('common.subtotal') }}</span>
                <span class="font-semibold">{{
                  formatPrice(subtotal)
                }}</span>
              </div>
              <div
                class="flex justify-between text-neutral-600 dark:text-neutral-300"
              >
                <span>{{ t('common.tax') }} (19%)</span>
                <span class="font-semibold">{{ formatPrice(tax) }}</span>
              </div>
              <UDivider />
              <div
                class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white"
              >
                <span>{{ t('common.total') }}</span>
                <span>{{ formatPrice(total) }}</span>
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
              :to="localePath('/tours')"
            >
              {{ t('cart.continue_shopping') }}
            </UButton>

            <!-- Security badges -->
            <div
              class="mt-6 pt-6 border-t border-neutral-200 dark:border-neutral-700"
            >
              <div
                class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-300 mb-2"
              >
                <UIcon
                  name="i-lucide-shield-check"
                  class="w-5 h-5 text-success"
                />
                <span>{{ t('cart.secure_payment') }}</span>
              </div>
              <div
                class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-300"
              >
                <UIcon
                  name="i-lucide-rotate-ccw"
                  class="w-5 h-5 text-info"
                />
                <span>{{ t('cart.free_cancellation') }}</span>
              </div>
            </div>
          </UCard>
        </div>
      </div>
    </UContainer>
  </div>
</template>
