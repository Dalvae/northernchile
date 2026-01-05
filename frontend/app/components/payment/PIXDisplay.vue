<script setup lang="ts">
import type { PaymentSessionRes } from '~/types/payment'
import { PaymentStatus } from '~/types/payment'

const { t } = useI18n()
const toast = useToast()
const router = useRouter()
const localePath = useLocalePath()
const paymentStore = usePaymentStore()

const props = defineProps<{
  payment: PaymentSessionRes
}>()

const emit = defineEmits<{
  success: []
  failed: [error: string]
  expired: []
  close: []
}>()

// Time remaining
const timeRemaining = ref('')
const isExpired = computed(() => {
  if (!props.payment.expiresAt) return false
  return new Date(props.payment.expiresAt) < new Date()
})

// Copy PIX code
const pixCodeCopied = ref(false)

async function copyPixCode() {
  if (!props.payment.pixCode) return

  try {
    await navigator.clipboard.writeText(props.payment.pixCode)
    pixCodeCopied.value = true
    toast.add({
      color: 'success',
      title: t('payment.pix.code_copied'),
      description: t('payment.pix.code_copied_description')
    })

    setTimeout(() => {
      pixCodeCopied.value = false
    }, 3000)
  } catch {
    toast.add({
      color: 'error',
      title: t('common.error'),
      description: t('payment.pix.copy_error')
    })
  }
}

// Update time remaining
function updateTimeRemaining() {
  if (!props.payment.expiresAt) {
    timeRemaining.value = ''
    return
  }

  const now = new Date().getTime()
  const expiry = new Date(props.payment.expiresAt).getTime()
  const diff = expiry - now

  if (diff <= 0) {
    timeRemaining.value = t('payment.pix.expired')
    emit('expired')
    return
  }

  const minutes = Math.floor(diff / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)

  timeRemaining.value = `${minutes}:${seconds.toString().padStart(2, '0')}`
}

// Start countdown and polling
const countdownInterval = ref<ReturnType<typeof setInterval> | null>(null)

onMounted(() => {
  // Update time remaining every second
  updateTimeRemaining()
  countdownInterval.value = setInterval(updateTimeRemaining, 1000)

  // Start polling payment status every 5 seconds
  paymentStore.startPolling(props.payment.sessionId, 5000)
})

onUnmounted(() => {
  if (countdownInterval.value) {
    clearInterval(countdownInterval.value)
  }
  paymentStore.stopPolling()
})

// Watch for payment status changes
watch(
  () => paymentStore.currentPayment?.status,
  (newStatus) => {
    if (!newStatus) return

    if (newStatus === PaymentStatus.Completed) {
      emit('success')
      toast.add({
        color: 'success',
        title: t('payment.pix.payment_received'),
        description: t('payment.pix.payment_received_description')
      })

      // Redirect to success page after short delay
      setTimeout(() => {
        router.push({
          path: localePath('/payment/callback'),
          query: {
            status: 'success',
            sessionId: props.payment.sessionId
          }
        })
      }, 1500)
    } else if (newStatus === PaymentStatus.Failed) {
      emit('failed', t('payment.pix.payment_failed'))
      toast.add({
        color: 'error',
        title: t('payment.pix.payment_failed'),
        description: t('payment.pix.payment_failed_description')
      })
    } else if (newStatus === PaymentStatus.Expired) {
      emit('expired')
      toast.add({
        color: 'warning',
        title: t('payment.pix.expired'),
        description: t('payment.pix.expired_description')
      })
    }
  }
)
</script>

<template>
  <div class="space-y-6">
    <!-- Header with close button -->
    <div class="relative">
      <UButton
        icon="i-lucide-x"
        color="neutral"
        variant="ghost"
        size="sm"
        class="absolute top-0 right-0"
        @click="emit('close')"
      />

      <div class="text-center">
        <div class="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-4">
          <UIcon
            name="i-lucide-qr-code"
            class="w-8 h-8 text-primary"
          />
        </div>
        <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('payment.pix.scan_qr_code') }}
        </h2>
        <p class="text-neutral-600 dark:text-neutral-300">
          {{ t('payment.pix.scan_description') }}
        </p>

        <!-- Test Mode Badge -->
        <div
          v-if="payment.isTest"
          class="inline-flex items-center gap-2 mt-3 px-3 py-1.5 bg-warning/10 border border-warning/30 rounded-full"
        >
          <UIcon
            name="i-lucide-flask-conical"
            class="w-4 h-4 text-warning"
          />
          <span class="text-sm font-semibold text-warning">
            {{ t('payment.test_mode') }}
          </span>
        </div>
      </div>
    </div>

    <!-- QR Code -->
    <div class="flex justify-center">
      <div class="p-6 bg-white dark:bg-neutral-800 rounded-xl border-2 border-neutral-200 dark:border-neutral-700 shadow-lg">
        <img
          v-if="payment.qrCode"
          :src="payment.qrCode"
          alt="PIX QR Code"
          class="w-64 h-64 object-contain"
        >
        <div
          v-else
          class="w-64 h-64 flex items-center justify-center bg-neutral-100 dark:bg-neutral-700 rounded-lg"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="w-12 h-12 text-neutral-300 animate-spin"
          />
        </div>
      </div>
    </div>

    <!-- Time Remaining -->
    <div
      v-if="timeRemaining"
      class="text-center"
    >
      <div
        class="inline-flex items-center gap-2 px-4 py-2 rounded-full"
        :class="[
          isExpired
            ? 'bg-error/10 text-error'
            : 'bg-warning/10 text-warning'
        ]"
      >
        <UIcon
          :name="isExpired ? 'i-lucide-x-circle' : 'i-lucide-clock'"
          class="w-5 h-5"
        />
        <span class="font-semibold text-sm">
          {{ isExpired ? t('payment.pix.expired') : `${t('payment.pix.expires_in')} ${timeRemaining}` }}
        </span>
      </div>
    </div>

    <!-- PIX Code (Copy-Paste) -->
    <div
      v-if="payment.pixCode"
      class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
    >
      <p class="text-sm font-medium text-neutral-900 dark:text-white mb-2">
        {{ t('payment.pix.copy_paste_option') }}
      </p>
      <div class="flex items-center gap-2">
        <div class="flex-1 p-3 bg-white dark:bg-neutral-700 rounded border border-neutral-200 dark:border-neutral-600 font-mono text-xs text-neutral-700 dark:text-neutral-200 overflow-hidden">
          <span class="block truncate">{{ payment.pixCode }}</span>
        </div>
        <UButton
          :icon="pixCodeCopied ? 'i-lucide-check' : 'i-lucide-copy'"
          :color="pixCodeCopied ? 'success' : 'primary'"
          size="lg"
          @click="copyPixCode"
        >
          {{ pixCodeCopied ? t('common.copied') : t('common.copy') }}
        </UButton>
      </div>
    </div>

    <!-- Instructions -->
    <div class="space-y-3">
      <h3 class="font-semibold text-neutral-900 dark:text-white">
        {{ t('payment.pix.how_to_pay') }}
      </h3>
      <ol class="space-y-2 text-sm text-neutral-600 dark:text-neutral-300">
        <li class="flex items-start gap-2">
          <span class="flex-shrink-0 w-6 h-6 bg-primary/20 text-primary rounded-full flex items-center justify-center text-xs font-semibold">
            1
          </span>
          <span>{{ t('payment.pix.step_1') }}</span>
        </li>
        <li class="flex items-start gap-2">
          <span class="flex-shrink-0 w-6 h-6 bg-primary/20 text-primary rounded-full flex items-center justify-center text-xs font-semibold">
            2
          </span>
          <span>{{ t('payment.pix.step_2') }}</span>
        </li>
        <li class="flex items-start gap-2">
          <span class="flex-shrink-0 w-6 h-6 bg-primary/20 text-primary rounded-full flex items-center justify-center text-xs font-semibold">
            3
          </span>
          <span>{{ t('payment.pix.step_3') }}</span>
        </li>
        <li class="flex items-start gap-2">
          <span class="flex-shrink-0 w-6 h-6 bg-primary/20 text-primary rounded-full flex items-center justify-center text-xs font-semibold">
            4
          </span>
          <span>{{ t('payment.pix.step_4') }}</span>
        </li>
      </ol>
    </div>

    <!-- Payment Status Indicator -->
    <div class="p-4 bg-primary/5 rounded-lg">
      <div class="flex items-center gap-3">
        <UIcon
          name="i-lucide-loader-2"
          class="w-5 h-5 text-primary animate-spin flex-shrink-0"
        />
        <div class="text-sm">
          <p class="font-medium text-neutral-900 dark:text-white">
            {{ t('payment.pix.waiting_payment') }}
          </p>
          <p class="text-neutral-600 dark:text-neutral-300">
            {{ t('payment.pix.automatic_confirmation') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Support Banks -->
    <div class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
      <p class="text-xs text-neutral-500 dark:text-neutral-300 text-center">
        <UIcon
          name="i-lucide-info"
          class="w-3 h-3 inline mr-1"
        />
        {{ t('payment.pix.supported_banks') }}
      </p>
    </div>
  </div>
</template>
