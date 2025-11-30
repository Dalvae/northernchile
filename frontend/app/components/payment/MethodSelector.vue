<script setup lang="ts">
import { PaymentProvider, PaymentMethod } from '~/types/payment'

const { t } = useI18n()

interface PaymentOption {
  provider: PaymentProvider
  method: PaymentMethod
  name: string
  description: string
  icon: string
  country: string
  available: boolean
}

const props = defineProps<{
  modelValue: {
    provider: PaymentProvider
    method: PaymentMethod
  } | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: {
    provider: PaymentProvider
    method: PaymentMethod
  }]
}>()

const paymentOptions: PaymentOption[] = [
  {
    provider: PaymentProvider.TRANSBANK,
    method: PaymentMethod.WEBPAY,
    name: 'Webpay Plus',
    description: t('payment.methods.transbank.description'),
    icon: '/assets/images/payment-methods/webpay.svg',
    country: 'Chile',
    available: true
  },
  {
    provider: PaymentProvider.MERCADOPAGO,
    method: PaymentMethod.CREDIT_CARD,
    name: 'Mercado Pago',
    description: t('payment.methods.mercadopago.description'),
    icon: '/assets/images/payment-methods/mercadopago.svg',
    country: t('payment.methods.mercadopago.countries'),
    available: true
  }
]

const selectedOption = computed({
  get: () => props.modelValue,
  set: (value) => {
    if (value) {
      emit('update:modelValue', value)
    }
  }
})

function selectMethod(option: PaymentOption) {
  if (!option.available) return

  selectedOption.value = {
    provider: option.provider,
    method: option.method
  }
}

function isSelected(option: PaymentOption) {
  return (
    selectedOption.value?.provider === option.provider
    && selectedOption.value?.method === option.method
  )
}
</script>

<template>
  <div class="space-y-4">
    <div>
      <h3 class="text-lg font-semibold text-neutral-900 dark:text-white mb-2">
        {{ t('payment.select_method') }}
      </h3>
      <p class="text-sm text-neutral-600 dark:text-neutral-300">
        {{ t('payment.select_method_description') }}
      </p>
    </div>

    <div
      class="space-y-3"
      role="radiogroup"
      aria-label="Seleccionar método de pago"
    >
      <div
        v-for="option in paymentOptions"
        :key="`${option.provider}-${option.method}`"
        class="relative p-4 rounded-lg border-2 transition-all cursor-pointer"
        :class="[
          isSelected(option)
            ? 'border-primary bg-primary/5'
            : option.available
              ? 'border-neutral-200 dark:border-neutral-700 hover:border-primary/50 hover:bg-neutral-50 dark:hover:bg-neutral-800'
              : 'border-neutral-200 dark:border-neutral-700 opacity-60 cursor-not-allowed',
          !option.available && 'grayscale'
        ]"
        role="radio"
        :aria-checked="isSelected(option)"
        :aria-disabled="!option.available"
        :aria-label="`${option.name} - ${option.description}`"
        tabindex="0"
        @click="selectMethod(option)"
        @keydown.enter="selectMethod(option)"
        @keydown.space.prevent="selectMethod(option)"
      >
        <div class="flex items-start gap-4">
          <!-- Logo -->
          <div
            class="flex-shrink-0 w-16 h-12 rounded-lg flex items-center justify-center overflow-hidden bg-white dark:bg-neutral-800 p-2"
          >
            <img
              :src="option.icon"
              :alt="option.name"
              class="w-full h-full object-contain"
            >
          </div>

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between gap-2 mb-1">
              <h4 class="font-semibold text-neutral-900 dark:text-white">
                {{ option.name }}
              </h4>
              <UBadge
                v-if="!option.available"
                color="neutral"
                size="sm"
              >
                {{ t('common.coming_soon') }}
              </UBadge>
            </div>
            <p class="text-sm text-neutral-600 dark:text-neutral-300 mb-2">
              {{ option.description }}
            </p>
            <div class="flex items-center gap-3 text-xs text-neutral-500 dark:text-neutral-300">
              <span class="flex items-center gap-1">
                <UIcon
                  name="i-lucide-map-pin"
                  class="w-3 h-3"
                />
                {{ option.country }}
              </span>
            </div>
          </div>

          <!-- Selection Indicator -->
          <div
            v-if="isSelected(option)"
            class="flex-shrink-0"
          >
            <div class="w-6 h-6 rounded-full bg-primary flex items-center justify-center">
              <UIcon
                name="i-lucide-check"
                class="w-4 h-4 text-white"
              />
            </div>
          </div>
        </div>

        <!-- Additional Info for Webpay -->
        <div
          v-if="option.method === PaymentMethod.WEBPAY && isSelected(option)"
          class="mt-3 pt-3 border-t border-neutral-200 dark:border-neutral-700"
        >
          <div class="flex items-start gap-2 text-xs text-neutral-600 dark:text-neutral-300">
            <UIcon
              name="i-lucide-info"
              class="w-4 h-4 flex-shrink-0 mt-0.5 text-primary"
            />
            <p>
              {{ t('payment.methods.transbank.info') }}
            </p>
          </div>
        </div>

        <!-- Additional Info for Credit Card (MercadoPago) -->
        <div
          v-if="option.method === PaymentMethod.CREDIT_CARD && isSelected(option)"
          class="mt-3 pt-3 border-t border-neutral-200 dark:border-neutral-700"
        >
          <div class="flex items-start gap-2 text-xs text-neutral-600 dark:text-neutral-300">
            <UIcon
              name="i-lucide-info"
              class="w-4 h-4 flex-shrink-0 mt-0.5 text-primary"
            />
            <p>
              {{ t('payment.methods.mercadopago.info') }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Security Notice -->
    <div class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
      <div class="flex items-start gap-3">
        <UIcon
          name="i-lucide-shield-check"
          class="w-5 h-5 text-success flex-shrink-0 mt-0.5"
        />
        <div class="text-sm">
          <p class="font-medium text-neutral-900 dark:text-white mb-1">
            {{ t('payment.security.title') }}
          </p>
          <p class="text-neutral-600 dark:text-neutral-300">
            {{ t('payment.security.description') }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
