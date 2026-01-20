<script setup lang="ts">
import type { CartItemRes } from 'api-client'

interface Props {
  currentCart: CartItemRes[]
  savedCart: CartItemRes[]
  open: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  select: ['current' | 'saved' | 'merge']
  close: []
}>()

const { t } = useI18n()
const { formatPrice } = useCurrency()

// Calculate total for a cart
function calculateTotal(items: CartItemRes[]): number {
  return items.reduce((sum, item) => sum + (item.itemTotal || 0), 0)
}
</script>

<template>
  <UModal
    :open="open"
    @update:open="(val: boolean) => !val && emit('close')"
  >
    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-center pb-4 border-b border-neutral-200 dark:border-neutral-700">
          <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
            {{ t('checkout.cart_conflict_title') }}
          </h3>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="sm"
            @click="emit('close')"
          />
        </div>

        <!-- Description -->
        <p class="text-sm text-neutral-600 dark:text-neutral-300 mt-4">
          {{ t('checkout.cart_conflict_description') }}
        </p>

        <!-- Cart comparison -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
          <!-- Current Cart -->
          <div class="p-4 border border-neutral-200 dark:border-neutral-700 rounded-lg">
            <h4 class="font-medium text-neutral-900 dark:text-white mb-3 flex items-center gap-2">
              <UIcon
                name="i-lucide-shopping-cart"
                class="w-4 h-4"
              />
              {{ t('checkout.cart_current') }}
            </h4>
            <div class="space-y-2 max-h-48 overflow-y-auto">
              <div
                v-for="item in currentCart"
                :key="item.itemId || item.scheduleId"
                class="text-sm"
              >
                <p class="text-neutral-900 dark:text-white font-medium truncate">
                  {{ item.tourName }}
                </p>
                <p class="text-neutral-500 dark:text-neutral-400 text-xs">
                  {{ item.numParticipants }} {{ item.numParticipants === 1 ? t('cart.person') : t('cart.persons') }}
                  · {{ formatPrice(item.itemTotal || 0) }}
                </p>
              </div>
              <div
                v-if="currentCart.length === 0"
                class="text-neutral-500 dark:text-neutral-400 text-sm italic"
              >
                {{ t('cart.empty') }}
              </div>
            </div>
            <div class="mt-3 pt-3 border-t border-neutral-200 dark:border-neutral-700">
              <p class="font-semibold text-neutral-900 dark:text-white">
                {{ t('common.total') }}: {{ formatPrice(calculateTotal(currentCart)) }}
              </p>
            </div>
          </div>

          <!-- Saved Cart -->
          <div class="p-4 border border-neutral-200 dark:border-neutral-700 rounded-lg">
            <h4 class="font-medium text-neutral-900 dark:text-white mb-3 flex items-center gap-2">
              <UIcon
                name="i-lucide-archive"
                class="w-4 h-4"
              />
              {{ t('checkout.cart_saved') }}
            </h4>
            <div class="space-y-2 max-h-48 overflow-y-auto">
              <div
                v-for="item in savedCart"
                :key="item.itemId || item.scheduleId"
                class="text-sm"
              >
                <p class="text-neutral-900 dark:text-white font-medium truncate">
                  {{ item.tourName }}
                </p>
                <p class="text-neutral-500 dark:text-neutral-400 text-xs">
                  {{ item.numParticipants }} {{ item.numParticipants === 1 ? t('cart.person') : t('cart.persons') }}
                  · {{ formatPrice(item.itemTotal || 0) }}
                </p>
              </div>
              <div
                v-if="savedCart.length === 0"
                class="text-neutral-500 dark:text-neutral-400 text-sm italic"
              >
                {{ t('cart.empty') }}
              </div>
            </div>
            <div class="mt-3 pt-3 border-t border-neutral-200 dark:border-neutral-700">
              <p class="font-semibold text-neutral-900 dark:text-white">
                {{ t('common.total') }}: {{ formatPrice(calculateTotal(savedCart)) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex flex-col sm:flex-row gap-3 mt-6 pt-4 border-t border-neutral-200 dark:border-neutral-700">
          <UButton
            color="neutral"
            variant="soft"
            class="flex-1"
            @click="emit('select', 'current')"
          >
            {{ t('checkout.use_current') }}
          </UButton>
          <UButton
            color="neutral"
            variant="soft"
            class="flex-1"
            @click="emit('select', 'saved')"
          >
            {{ t('checkout.use_saved') }}
          </UButton>
          <UButton
            color="primary"
            class="flex-1"
            @click="emit('select', 'merge')"
          >
            {{ t('checkout.merge_carts') }}
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
