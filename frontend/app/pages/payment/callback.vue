<script setup lang="ts">
import { PaymentStatus } from '~/types/payment'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const toast = useToast()
const paymentStore = usePaymentStore()
const cartStore = useCartStore()
const { formatDateTime } = useDateTime()

// SEO: Prevent indexing of payment callback page
useHead({
  meta: [
    { name: 'robots', content: 'noindex, nofollow' }
  ]
})

// Page state
const isLoading = ref(true)
const paymentStatus = ref<PaymentStatus | null>(null)
const paymentId = ref<string | null>(null)
const bookingId = ref<string | null>(null)
const errorMessage = ref<string | undefined>(undefined)

// Check query params for different providers
const queryStatus = route.query.status as string
const tokenWs = route.query.token_ws as string

// MercadoPago specific params
const mpCollectionStatus = route.query.collection_status as string
const mpPaymentId = route.query.payment_id as string
const mpExternalReference = route.query.external_reference as string
const mpPreferenceId = route.query.preference_id as string

// Clear checkout data from localStorage (same keys as checkout.vue)
function clearCheckoutData() {
  if (import.meta.client) {
    localStorage.removeItem('checkout_contact')
    localStorage.removeItem('checkout_participants')
    localStorage.removeItem('checkout_step')
  }
}

// Process payment callback on mount
onMounted(async () => {
  try {
    // Case 1: Transbank redirect with token_ws
    if (tokenWs) {
      await handleTransbankCallback(tokenWs)
    }
    // Case 2: MercadoPago Checkout Pro redirect
    else if (mpCollectionStatus || mpPaymentId) {
      await handleMercadoPagoCallback()
    }
    // Case 3: Direct success/error from query (PIX polling success)
    else if (queryStatus) {
      handleDirectCallback()
    }
    // Case 4: No valid params, redirect to home
    else {
      toast.add({
        color: 'warning',
        title: t('payment.callback.invalid_callback'),
        description: t('payment.callback.no_payment_data')
      })
      setTimeout(() => router.push('/'), 2000)
    }
  } catch (error: any) {
    console.error('Error processing payment callback:', error)
    errorMessage.value = error.message || t('payment.callback.error_processing')
    paymentStatus.value = PaymentStatus.FAILED
  } finally {
    isLoading.value = false
  }
})

async function handleTransbankCallback(token: string) {
  try {
    // Confirm payment with backend
    const result = await paymentStore.confirmPayment(token)

    paymentId.value = result.paymentId
    paymentStatus.value = result.status
    bookingId.value = route.query.bookingId as string || null

    if (result.status === PaymentStatus.COMPLETED) {
      // Google Analytics: Track purchase event
      if (typeof window !== 'undefined' && window.gtag && result.amount) {
        window.gtag('event', 'purchase', {
          transaction_id: result.paymentId,
          value: result.amount,
          currency: result.currency || 'CLP'
        })
      }

      // Clear cart after successful payment
      cartStore.clearCart()
      clearCheckoutData()

      toast.add({
        color: 'success',
        title: t('payment.success.title'),
        description: t('payment.success.description')
      })
    } else if (result.status === PaymentStatus.FAILED) {
      errorMessage.value = result.message || t('payment.error.transaction_failed')
      toast.add({
        color: 'error',
        title: t('payment.error.title'),
        description: errorMessage.value
      })
    }
  } catch (error: any) {
    console.error('Error confirming Transbank payment:', error)
    paymentStatus.value = PaymentStatus.FAILED
    errorMessage.value = error.message || t('payment.error.confirmation_failed')
    toast.add({
      color: 'error',
      title: t('payment.error.title'),
      description: errorMessage.value
    })
  }
}

async function handleMercadoPagoCallback() {
  try {
    // Call backend to confirm the MercadoPago payment
    // This will update booking status and send confirmation emails
    const config = useRuntimeConfig()
    
    const queryParams = new URLSearchParams()
    if (mpPreferenceId) queryParams.append('preference_id', mpPreferenceId)
    if (mpPaymentId) queryParams.append('payment_id', mpPaymentId)
    if (mpCollectionStatus) queryParams.append('collection_status', mpCollectionStatus)
    
    const result = await $fetch<{ paymentId: string; status: PaymentStatus; amount?: number; currency?: string }>(
      `/api/payments/confirm/mercadopago?${queryParams.toString()}`,
      {
        credentials: 'include'
      }
    )

    paymentId.value = result.paymentId
    paymentStatus.value = result.status
    bookingId.value = mpExternalReference || route.query.bookingId as string || null

    if (result.status === PaymentStatus.COMPLETED) {
      // Google Analytics: Track purchase event
      if (typeof window !== 'undefined' && window.gtag) {
        window.gtag('event', 'purchase', {
          transaction_id: result.paymentId || 'unknown',
          value: result.amount || 0,
          currency: result.currency || 'USD'
        })
      }

      // Clear cart after successful payment
      cartStore.clearCart()
      clearCheckoutData()

      toast.add({
        color: 'success',
        title: t('payment.success.title'),
        description: t('payment.success.description')
      })
    } else if (result.status === PaymentStatus.FAILED) {
      errorMessage.value = t('payment.error.transaction_failed')
      toast.add({
        color: 'error',
        title: t('payment.error.title'),
        description: errorMessage.value
      })
    } else if (result.status === PaymentStatus.CANCELLED) {
      errorMessage.value = t('payment.error.cancelled_by_user')
      toast.add({
        color: 'warning',
        title: t('payment.error.cancelled'),
        description: errorMessage.value
      })
    } else if (result.status === PaymentStatus.PENDING) {
      toast.add({
        color: 'info',
        title: t('payment.callback.pending_title'),
        description: t('payment.callback.pending_description')
      })
    }
  } catch (error: any) {
    console.error('Error handling MercadoPago callback:', error)
    
    // Fallback: Map collection_status if backend call fails
    const statusMap: Record<string, PaymentStatus> = {
      approved: PaymentStatus.COMPLETED,
      pending: PaymentStatus.PENDING,
      in_process: PaymentStatus.PROCESSING,
      rejected: PaymentStatus.FAILED,
      cancelled: PaymentStatus.CANCELLED,
      refunded: PaymentStatus.REFUNDED,
      null: PaymentStatus.PENDING
    }

    paymentStatus.value = statusMap[mpCollectionStatus] || PaymentStatus.PENDING
    paymentId.value = mpPaymentId || mpPreferenceId || null
    bookingId.value = mpExternalReference || route.query.bookingId as string || null
    errorMessage.value = error.message || t('payment.error.confirmation_failed')
  }
}

function handleDirectCallback() {
  paymentId.value = route.query.paymentId as string || null
  bookingId.value = route.query.bookingId as string || null

  if (queryStatus === 'success') {
    paymentStatus.value = PaymentStatus.COMPLETED

    // Google Analytics: Track purchase event (direct callback)
    if (typeof window !== 'undefined' && window.gtag) {
      const amount = parseFloat(route.query.amount as string) || 0
      window.gtag('event', 'purchase', {
        transaction_id: paymentId.value || 'unknown',
        value: amount,
        currency: 'CLP'
      })
    }

    // Clear cart after successful payment
    cartStore.clearCart()
    clearCheckoutData()
  } else if (queryStatus === 'error' || queryStatus === 'failed') {
    paymentStatus.value = PaymentStatus.FAILED
    errorMessage.value = route.query.message as string || t('payment.error.generic')
  } else if (queryStatus === 'cancelled') {
    paymentStatus.value = PaymentStatus.CANCELLED
    errorMessage.value = t('payment.error.cancelled_by_user')
  }
}

const isSuccess = computed(() => paymentStatus.value === PaymentStatus.COMPLETED)
const isFailed = computed(() =>
  paymentStatus.value === PaymentStatus.FAILED
  || paymentStatus.value === PaymentStatus.CANCELLED
)

function goToBookings() {
  router.push('/profile/bookings')
}

function goToHome() {
  router.push('/')
}

function retryPayment() {
  router.push('/checkout')
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 flex items-center justify-center py-12">
    <UContainer>
      <div class="max-w-2xl mx-auto">
        <!-- Loading State -->
        <div
          v-if="isLoading"
          class="text-center"
        >
          <div class="w-20 h-20 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <UIcon
              name="i-lucide-loader-2"
              class="w-12 h-12 text-primary animate-spin"
            />
          </div>
          <h1 class="text-2xl font-bold text-neutral-900 dark:text-white mb-2">
            {{ t('payment.callback.processing') }}
          </h1>
          <p class="text-neutral-600 dark:text-neutral-300">
            {{ t('payment.callback.verifying_payment') }}
          </p>
        </div>

        <!-- Success State -->
        <div
          v-else-if="isSuccess"
          class="text-center"
        >
          <div class="mb-6">
            <div class="w-20 h-20 bg-success/10 rounded-full flex items-center justify-center mx-auto mb-4">
              <UIcon
                name="i-lucide-check-circle"
                class="w-12 h-12 text-success"
              />
            </div>
            <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
              {{ t('payment.success.title') }}
            </h1>
            <p class="text-lg text-neutral-600 dark:text-neutral-300">
              {{ t('payment.success.subtitle') }}
            </p>
          </div>

          <UCard class="text-left">
            <div class="space-y-4">
              <div
                v-if="paymentId"
                class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
              >
                <p class="text-sm text-neutral-500 dark:text-neutral-300 mb-1">
                  {{ t('payment.callback.payment_id') }}
                </p>
                <p class="text-lg font-bold text-neutral-900 dark:text-white font-mono">
                  {{ paymentId }}
                </p>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <p class="text-neutral-500 dark:text-neutral-300 mb-1">
                    {{ t('common.status') }}
                  </p>
                  <UBadge
                    color="success"
                    size="lg"
                  >
                    {{ t('payment.status.completed') }}
                  </UBadge>
                </div>
                <div>
                  <p class="text-neutral-500 dark:text-neutral-300 mb-1">
                    {{ t('payment.callback.payment_date') }}
                  </p>
                  <p class="font-medium text-neutral-900 dark:text-white">
                    {{ formatDateTime(new Date()) }}
                  </p>
                </div>
              </div>

              <UDivider />

              <div class="space-y-3">
                <div class="flex items-start gap-2 text-sm">
                  <UIcon
                    name="i-lucide-mail"
                    class="w-5 h-5 text-primary mt-0.5 flex-shrink-0"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      {{ t('payment.callback.confirmation_sent_title') }}
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-300">
                      {{ t('payment.callback.confirmation_sent_description') }}
                    </p>
                  </div>
                </div>

                <div class="flex items-start gap-2 text-sm">
                  <UIcon
                    name="i-lucide-calendar"
                    class="w-5 h-5 text-primary mt-0.5 flex-shrink-0"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      {{ t('payment.callback.booking_confirmed_title') }}
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-300">
                      {{ t('payment.callback.booking_confirmed_description') }}
                    </p>
                  </div>
                </div>

                <div class="flex items-start gap-2 text-sm">
                  <UIcon
                    name="i-lucide-info"
                    class="w-5 h-5 text-primary mt-0.5 flex-shrink-0"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      {{ t('payment.callback.cancellation_policy_title') }}
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-300">
                      {{ t('payment.callback.cancellation_policy_description') }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex flex-col sm:flex-row gap-3">
                <UButton
                  color="primary"
                  size="lg"
                  block
                  icon="i-lucide-list"
                  @click="goToBookings"
                >
                  {{ t('payment.callback.view_bookings_button') }}
                </UButton>
                <UButton
                  color="neutral"
                  variant="outline"
                  size="lg"
                  block
                  icon="i-lucide-home"
                  @click="goToHome"
                >
                  {{ t('payment.callback.back_to_home_button') }}
                </UButton>
              </div>
            </template>
          </UCard>
        </div>

        <!-- Error State -->
        <div
          v-else-if="isFailed"
          class="text-center"
        >
          <div class="mb-6">
            <div class="w-20 h-20 bg-error/10 rounded-full flex items-center justify-center mx-auto mb-4">
              <UIcon
                name="i-lucide-x-circle"
                class="w-12 h-12 text-error"
              />
            </div>
            <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
              {{ t('payment.error.title') }}
            </h1>
            <p class="text-lg text-neutral-600 dark:text-neutral-300">
              {{ paymentStatus === PaymentStatus.CANCELLED ? t('payment.error.cancelled') : t('payment.error.subtitle') }}
            </p>
          </div>

          <UCard class="text-left">
            <div class="space-y-4">
              <div
                v-if="errorMessage"
                class="p-4 bg-error/5 border border-error/20 rounded-lg"
              >
                <p class="text-sm text-error font-medium">
                  {{ errorMessage }}
                </p>
              </div>

              <div class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
                <h3 class="font-medium text-neutral-900 dark:text-white mb-3">
                  {{ t('payment.callback.possible_reasons_title') }}
                </h3>
                <ul class="space-y-2 text-sm text-neutral-600 dark:text-neutral-300">
                  <li class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-circle"
                      class="w-1.5 h-1.5 fill-current"
                    />
                    {{ t('payment.callback.reason_insufficient_funds') }}
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-circle"
                      class="w-1.5 h-1.5 fill-current"
                    />
                    {{ t('payment.callback.reason_expired_card') }}
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-circle"
                      class="w-1.5 h-1.5 fill-current"
                    />
                    {{ t('payment.callback.reason_incorrect_data') }}
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-circle"
                      class="w-1.5 h-1.5 fill-current"
                    />
                    {{ t('payment.callback.reason_transaction_limit') }}
                  </li>
                  <li
                    v-if="paymentStatus === PaymentStatus.CANCELLED"
                    class="flex items-center gap-2"
                  >
                    <UIcon
                      name="i-lucide-circle"
                      class="w-1.5 h-1.5 fill-current"
                    />
                    {{ t('payment.callback.reason_user_cancelled') }}
                  </li>
                </ul>
              </div>

              <div class="p-4 bg-primary/5 border border-primary/20 rounded-lg">
                <div class="flex items-start gap-2 text-sm">
                  <UIcon
                    name="i-lucide-info"
                    class="w-4 h-4 text-primary flex-shrink-0 mt-0.5"
                  />
                  <p class="text-neutral-700 dark:text-neutral-200">
                    {{ t('payment.callback.support_message') }}
                  </p>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex flex-col sm:flex-row gap-3">
                <UButton
                  color="primary"
                  size="lg"
                  block
                  icon="i-lucide-rotate-ccw"
                  @click="retryPayment"
                >
                  {{ t('payment.callback.try_again_button') }}
                </UButton>
                <UButton
                  color="neutral"
                  variant="outline"
                  size="lg"
                  block
                  icon="i-lucide-home"
                  @click="goToHome"
                >
                  {{ t('payment.callback.back_to_home_button') }}
                </UButton>
              </div>
            </template>
          </UCard>
        </div>
      </div>
    </UContainer>
  </div>
</template>
