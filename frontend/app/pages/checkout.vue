<script setup lang="ts">
import type { PaymentProvider } from '~/types/payment'
import { PaymentMethod } from '~/types/payment'

const cartStore = useCartStore()
const authStore = useAuthStore()
const paymentStore = usePaymentStore()
const router = useRouter()
const toast = useToast()
const { locale, t } = useI18n()
const localePath = useLocalePath()
const { phoneCodes, getCountryFlag } = useCountries()
const { formatPrice } = useCurrency()

// SEO: Prevent indexing of checkout page
useHead({
  meta: [
    { name: 'robots', content: 'noindex, nofollow' }
  ]
})

// Redirect if cart is empty
if (cartStore.cart.items.length === 0) {
  router.push(localePath('/cart'))
}

// Wizard steps
const currentStep = ref(1)
const totalSteps = 3

// Participant type for reuse
interface Participant {
  fullName: string
  documentId: string
  nationality: string
  dateOfBirth: string | null
  pickupAddress: string
  specialRequirements: string
  phoneCountryCode: string
  phoneNumber: string
  email: string
}

// Step 1: Contact Information
const contactForm = ref({
  email: authStore.user?.email || '',
  fullName: authStore.user?.fullName || '',
  phone: '',
  countryCode: '+56',
  password: '',
  confirmPassword: ''
})

// Step 2: Participants
const participants = ref<Participant[]>([])

// LocalStorage keys for checkout persistence
const CHECKOUT_CONTACT_KEY = 'checkout_contact'
const CHECKOUT_PARTICIPANTS_KEY = 'checkout_participants'
const CHECKOUT_STEP_KEY = 'checkout_step'

// Load saved checkout data on mount
onMounted(() => {
  try {
    // Load contact form (except passwords)
    const savedContact = localStorage.getItem(CHECKOUT_CONTACT_KEY)
    if (savedContact) {
      const parsed = JSON.parse(savedContact)
      contactForm.value = {
        ...contactForm.value,
        email: parsed.email || contactForm.value.email,
        fullName: parsed.fullName || contactForm.value.fullName,
        phone: parsed.phone || '',
        countryCode: parsed.countryCode || '+56',
        password: '',
        confirmPassword: ''
      }
    }

    // Load participants
    const savedParticipants = localStorage.getItem(CHECKOUT_PARTICIPANTS_KEY)
    if (savedParticipants) {
      const parsed = JSON.parse(savedParticipants) as Participant[]
      // Only restore if participant count matches current cart
      if (parsed.length === cartStore.totalItems) {
        participants.value = parsed
      }
    }

    // Load step
    const savedStep = localStorage.getItem(CHECKOUT_STEP_KEY)
    if (savedStep) {
      const step = parseInt(savedStep, 10)
      if (step >= 1 && step <= totalSteps) {
        currentStep.value = step
      }
    }
  } catch (e) {
    console.error('Error loading checkout data from localStorage:', e)
  }
})

// Save contact form when it changes (debounced via watch)
watch(contactForm, (newVal) => {
  if (import.meta.client) {
    // Don't save passwords
    const toSave = {
      email: newVal.email,
      fullName: newVal.fullName,
      phone: newVal.phone,
      countryCode: newVal.countryCode
    }
    localStorage.setItem(CHECKOUT_CONTACT_KEY, JSON.stringify(toSave))
  }
}, { deep: true })

// Save participants when they change
watch(participants, (newVal) => {
  if (import.meta.client && newVal.length > 0) {
    localStorage.setItem(CHECKOUT_PARTICIPANTS_KEY, JSON.stringify(newVal))
  }
}, { deep: true })

// Save current step
watch(currentStep, (newVal) => {
  if (import.meta.client) {
    localStorage.setItem(CHECKOUT_STEP_KEY, String(newVal))
  }
})

// Clear checkout data from localStorage (call after successful payment)
function clearCheckoutData() {
  if (import.meta.client) {
    localStorage.removeItem(CHECKOUT_CONTACT_KEY)
    localStorage.removeItem(CHECKOUT_PARTICIPANTS_KEY)
    localStorage.removeItem(CHECKOUT_STEP_KEY)
  }
}

// Initialize participants based on total count
const totalParticipants = computed(() => cartStore.totalItems)

function initializeParticipants() {
  // Only initialize if participants are empty or count changed
  if (participants.value.length === totalParticipants.value) {
    return
  }
  participants.value = Array.from(
    { length: totalParticipants.value },
    (_, i) => ({
      fullName: i === 0 ? contactForm.value.fullName : '',
      documentId: '',
      nationality: 'CL',
      dateOfBirth: null,
      pickupAddress: '',
      specialRequirements: '',
      phoneCountryCode: contactForm.value.countryCode,
      phoneNumber: i === 0 ? contactForm.value.phone : '',
      email: i === 0 ? contactForm.value.email : ''
    })
  )
}

// Step 3: Payment
const selectedPaymentMethod = ref<{ provider: PaymentProvider, method: PaymentMethod } | null>(null)
const showPIXModal = ref(false)

// Validation
const step1Valid = computed(() => {
  const baseValidation
    = contactForm.value.email
      && contactForm.value.fullName
      && contactForm.value.phone.length >= 6

  if (authStore.isAuthenticated) {
    return baseValidation
  }

  const passwordValidation
    = contactForm.value.password.length >= 8
      && contactForm.value.password === contactForm.value.confirmPassword

  return baseValidation && passwordValidation
})

const step2Valid = computed(() => {
  return participants.value.every(
    p => p.fullName && p.documentId && p.nationality
  )
})

// Navigation
function nextStep() {
  if (currentStep.value === 1 && !step1Valid.value) {
    toast.add({
      color: 'warning',
      title: t('checkout.validation.incomplete_info'),
      description: t('checkout.validation.complete_required_fields')
    })
    return
  }

  if (currentStep.value === 1) {
    initializeParticipants()
  }

  if (currentStep.value === 2 && !step2Valid.value) {
    toast.add({
      color: 'warning',
      title: t('checkout.validation.incomplete_info'),
      description: t('checkout.validation.complete_all_participants')
    })
    return
  }

  if (currentStep.value < totalSteps) {
    currentStep.value++
  }
}

function prevStep() {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// Update participant data
function updateParticipant(index: number, data: Partial<{
  fullName: string
  documentId: string
  nationality: string
  dateOfBirth: string | null
  pickupAddress: string
  specialRequirements: string
  phoneCountryCode: string
  phoneNumber: string
  email: string
}>) {
  const current = participants.value[index]
  if (!current) return
  participants.value[index] = {
    ...current,
    ...data
  }
}

// Copy first participant's common data to another participant
function copyFromFirstParticipant(index: number) {
  const first = participants.value[0]
  if (!first || index === 0) return

  const current = participants.value[index]
  if (!current) return

  // Copy pickup address, special requirements, and phone country code (common for all participants)
  participants.value[index] = {
    ...current,
    pickupAddress: first.pickupAddress,
    specialRequirements: first.specialRequirements,
    phoneCountryCode: first.phoneCountryCode
  }
}

// Submit payment - creates PaymentSession, then redirects to payment provider
const isSubmitting = ref(false)
const lastSubmitTime = ref(0)

// PaymentSession response type
interface PaymentSessionRes {
  sessionId: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'EXPIRED' | 'CANCELLED'
  paymentUrl?: string
  token?: string
  qrCode?: string
  pixCode?: string
  expiresAt?: string
  isTest?: boolean
  bookingIds?: string[]
}

async function submitBooking() {
  // Prevent double submit - strict check
  if (isSubmitting.value) {
    console.warn('Submit already in progress, ignoring')
    return
  }

  // Prevent rapid double-click
  const now = Date.now()
  if (now - lastSubmitTime.value < 3000) {
    console.warn('Submit too fast, ignoring')
    return
  }
  lastSubmitTime.value = now

  if (!selectedPaymentMethod.value) return
  isSubmitting.value = true

  const config = useRuntimeConfig()

  try {
    // Step 1: Ensure user is authenticated
    if (!authStore.isAuthenticated) {
      toast.add({
        color: 'info',
        title: t('auth.login'),
        description: t('auth.register_description')
      })

      // Register and login the new user
      try {
        await authStore.register({
          email: contactForm.value.email,
          password: contactForm.value.password,
          fullName: contactForm.value.fullName,
          phoneNumber: contactForm.value.phone ? `${contactForm.value.countryCode}${contactForm.value.phone}` : null,
          nationality: participants.value[0]?.nationality || null
        })

        // Auto-login after registration
        await authStore.login({
          email: contactForm.value.email,
          password: contactForm.value.password
        })

        // Verify login was successful before continuing
        if (!authStore.isAuthenticated) {
          throw new Error('Login failed after registration')
        }
      } catch (error: unknown) {
        const statusCode = error && typeof error === 'object' && 'statusCode' in error
          ? (error as { statusCode?: number }).statusCode
          : undefined

        if (statusCode === 409) {
          toast.add({
            color: 'warning',
            title: t('common.error'),
            description: t('checkout.toast.account_exists')
          })
          router.push(localePath('/auth'))
          return
        }
        throw error
      }
    }

    // Make a copy of cart items at the start (before any async operations that might clear the cart)
    const cartItems = JSON.parse(JSON.stringify(cartStore.cart.items)) as typeof cartStore.cart.items
    const cartTotal = cartStore.cart.cartTotal

    if (!cartItems || cartItems.length === 0) {
      throw new Error('No items in cart')
    }

    // Step 2: Build PaymentSession items with participants
    toast.add({
      color: 'info',
      title: t('checkout.toast.processing_booking'),
      description: t('checkout.toast.preparing_payment'),
      icon: 'i-lucide-loader-2'
    })

    let participantOffset = 0
    const sessionItems = cartItems.map((item) => {
      const itemParticipants = participants.value.slice(
        participantOffset,
        participantOffset + item.numParticipants
      )
      participantOffset += item.numParticipants

      return {
        scheduleId: String(item.scheduleId),
        tourName: item.tourName,
        numParticipants: item.numParticipants,
        pricePerPerson: item.pricePerParticipant,
        itemTotal: item.itemTotal ?? 0,
        specialRequests: null,
        participants: itemParticipants.map(p => ({
          fullName: p.fullName,
          documentId: p.documentId,
          nationality: p.nationality || null,
          dateOfBirth: p.dateOfBirth || null,
          pickupAddress: p.pickupAddress || null,
          specialRequirements: p.specialRequirements || null,
          phoneNumber: p.phoneNumber ? `${p.phoneCountryCode}${p.phoneNumber}` : null,
          email: p.email || null
        }))
      }
    })

    const tourNames = cartItems.map(item => item.tourName).join(', ')

    // Step 3: Create PaymentSession (no bookings created yet!)
    const paymentSessionRes = await $fetch<PaymentSessionRes>('/api/payment-sessions', {
      method: 'POST',
      body: {
        provider: selectedPaymentMethod.value.provider,
        paymentMethod: selectedPaymentMethod.value.method,
        totalAmount: cartTotal,
        currency: 'CLP',
        items: sessionItems,
        returnUrl: `${config.public.baseUrl}/payment/callback`,
        cancelUrl: `${config.public.baseUrl}/checkout`,
        userEmail: contactForm.value.email,
        description: `Reserva para ${tourNames}`,
        languageCode: locale.value
      },
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    // Store session ID for PIX polling
    if (paymentSessionRes.sessionId) {
      paymentStore.setCurrentPayment({
        paymentId: paymentSessionRes.sessionId,
        status: paymentSessionRes.status as any,
        qrCode: paymentSessionRes.qrCode,
        pixCode: paymentSessionRes.pixCode,
        expiresAt: paymentSessionRes.expiresAt
      })
    }

    // Step 4: Handle payment flow based on method
    if (selectedPaymentMethod.value.method === PaymentMethod.WEBPAY) {
      // Transbank Webpay - requires POST redirect with token_ws
      if (paymentSessionRes.paymentUrl && paymentSessionRes.token) {
        toast.add({
          color: 'success',
          title: t('checkout.toast.redirecting_payment'),
          description: t('checkout.toast.redirecting_provider', { provider: 'Transbank' })
        })

        // Clear checkout data before redirecting (payment initiated successfully)
        clearCheckoutData()

        // Transbank requires POST with token_ws parameter
        setTimeout(() => {
          const form = document.createElement('form')
          form.method = 'POST'
          form.action = paymentSessionRes.paymentUrl!

          const tokenInput = document.createElement('input')
          tokenInput.type = 'hidden'
          tokenInput.name = 'token_ws'
          tokenInput.value = paymentSessionRes.token!

          form.appendChild(tokenInput)
          document.body.appendChild(form)
          form.submit()
        }, 1500)
      } else {
        throw new Error('No payment URL or token received from Transbank')
      }
    } else if (selectedPaymentMethod.value.method === PaymentMethod.CREDIT_CARD) {
      // MercadoPago Checkout Pro - uses GET redirect
      if (paymentSessionRes.paymentUrl) {
        toast.add({
          color: 'success',
          title: t('checkout.toast.redirecting_payment'),
          description: t('checkout.toast.redirecting_provider', { provider: 'MercadoPago' })
        })

        // Clear checkout data before redirecting (payment initiated successfully)
        clearCheckoutData()

        // MercadoPago uses simple redirect
        setTimeout(() => {
          window.location.href = paymentSessionRes.paymentUrl!
        }, 1500)
      } else {
        throw new Error('No payment URL received from MercadoPago')
      }
    } else if (selectedPaymentMethod.value.method === PaymentMethod.PIX) {
      // PIX: Show QR code modal
      showPIXModal.value = true
    }
  } catch (error: unknown) {
    console.error('Error in checkout process:', error)

    let errorMessage = t('checkout.errors.processing_error')

    // Handle specific errors
    const errorData = error && typeof error === 'object' && 'data' in error
      ? (error as { data?: { message?: string } }).data
      : undefined

    if (errorData?.message) {
      if (errorData.message.includes('Not enough available slots')) {
        errorMessage = t('checkout.errors.no_slots_available')
      } else if (errorData.message.includes('not found')) {
        errorMessage = t('checkout.errors.tour_not_available')
      } else {
        errorMessage = errorData.message
      }
    }

    toast.add({
      color: 'error',
      title: t('common.error'),
      description: errorMessage
    })
  } finally {
    isSubmitting.value = false
  }
}

// Handle PIX payment success
function handlePIXSuccess() {
  showPIXModal.value = false
  clearCheckoutData()
  // Note: PIXDisplay component handles redirect to callback page
  // Cart will be cleared on the callback page after payment confirmation
}

// Handle PIX payment failure
function handlePIXFailed(error: string) {
  showPIXModal.value = false

  toast.add({
    color: 'error',
    title: t('payment.error.title'),
    description: error
  })
}

// Handle PIX payment expiration
function handlePIXExpired() {
  showPIXModal.value = false

  toast.add({
    color: 'warning',
    title: t('payment.pix.expired'),
    description: t('payment.pix.expired_description')
  })
}

// Calculate total - use value from backend cart API (tax-inclusive)
const total = computed(() => cartStore.cart.cartTotal)
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-8">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('checkout.title') }}
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300">
          {{ t('checkout.steps_description', { steps: totalSteps }) }}
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Progress Steps -->
          <div class="mb-8">
            <div class="flex items-center justify-between mb-4">
              <div
                v-for="step in totalSteps"
                :key="step"
                class="flex items-center"
                :class="{ 'flex-1': step < totalSteps }"
              >
                <div
                  class="flex items-center justify-center w-10 h-10 rounded-full font-semibold transition-colors"
                  :class="[
                    currentStep >= step
                      ? 'bg-primary text-white'
                      : 'bg-neutral-200 dark:bg-neutral-700 text-neutral-600 dark:text-neutral-300'
                  ]"
                >
                  {{ step }}
                </div>
                <div
                  v-if="step < totalSteps"
                  class="flex-1 h-1 mx-2"
                  :class="[
                    currentStep > step
                      ? 'bg-primary'
                      : 'bg-neutral-200 dark:bg-neutral-700'
                  ]"
                />
              </div>
            </div>
            <div class="flex justify-between text-sm">
              <span
                :class="
                  currentStep >= 1
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-300'
                "
              >
                {{ t('checkout.step_contact') }}
              </span>
              <span
                :class="
                  currentStep >= 2
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-300'
                "
              >
                {{ t('checkout.step_participants') }}
              </span>
              <span
                :class="
                  currentStep >= 3
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-300'
                "
              >
                {{ t('checkout.step_payment') }}
              </span>
            </div>
          </div>

          <!-- Step 1: Contact Information -->
          <UCard v-if="currentStep === 1">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t('checkout.contact_info_title') }}
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
                {{ t('checkout.contact_info_description') }}
              </p>
            </template>

            <div class="space-y-4">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                >
                  {{ t('checkout.full_name') }} *
                </label>
                <input
                  v-model="contactForm.fullName"
                  type="text"
                  :placeholder="t('checkout.full_name_placeholder')"
                  class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  required
                >
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                >
                  {{ t('checkout.email') }} *
                </label>
                <input
                  v-model="contactForm.email"
                  type="email"
                  placeholder="juan@example.com"
                  class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  required
                >
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                >
                  {{ t('checkout.phone') }} *
                </label>
                <div class="flex gap-2">
                  <select
                    v-model="contactForm.countryCode"
                    class="w-28 px-2 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent text-sm"
                  >
                    <option
                      v-for="pc in phoneCodes"
                      :key="pc.code + pc.country"
                      :value="pc.code"
                    >
                      {{ getCountryFlag(pc.country) }} {{ pc.code }}
                    </option>
                  </select>
                  <input
                    v-model="contactForm.phone"
                    type="tel"
                    placeholder="912345678"
                    class="flex-1 px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  >
                </div>
              </div>

              <div
                v-if="!authStore.isAuthenticated"
                class="space-y-4 mt-4"
              >
                <p class="text-sm text-neutral-600 dark:text-neutral-300">
                  {{ t('checkout.create_account_prompt') }} <NuxtLink
                    :to="{ path: localePath('/auth'), query: { redirect: localePath('/checkout') } }"
                    class="text-primary font-medium hover:underline"
                  >{{ t('checkout.login_here') }}</NuxtLink>.
                </p>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                    >
                      {{ t('checkout.password') }} *
                    </label>
                    <input
                      v-model="contactForm.password"
                      type="password"
                      placeholder="••••••••"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                      required
                    >
                  </div>
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                    >
                      {{ t('checkout.confirm_password') }} *
                    </label>
                    <input
                      v-model="contactForm.confirmPassword"
                      type="password"
                      placeholder="••••••••"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                      required
                    >
                  </div>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  :to="localePath('/cart')"
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                >
                  {{ t('checkout.back_to_cart') }}
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  :disabled="!step1Valid"
                  @click="nextStep"
                >
                  {{ t('checkout.continue') }}
                </UButton>
              </div>
            </template>
          </UCard>

          <!-- Step 2: Participants -->
          <UCard v-if="currentStep === 2">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t('checkout.participants_title') }}
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
                {{ totalParticipants }}
                {{ totalParticipants === 1 ? t('checkout.person') : t('checkout.persons') }} {{ t('common.total').toLowerCase() }}
              </p>
            </template>

            <div class="space-y-6">
              <div
                v-for="(participant, index) in participants"
                :key="index"
              >
                <BookingParticipantForm
                  :participant="participant"
                  :index="index"
                  :total-participants="totalParticipants"
                  @update="(data) => updateParticipant(index, data)"
                >
                  <template #header>
                    <div class="flex items-center justify-between">
                      <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
                        {{ t('checkout.participant_number', { number: index + 1 }) }}
                        <span
                          v-if="index === 0"
                          class="text-sm text-neutral-500 dark:text-neutral-300 font-normal ml-2"
                        >
                          {{ t('checkout.primary_contact') }}
                        </span>
                      </h3>
                      <UButton
                        v-if="index > 0"
                        size="xs"
                        color="neutral"
                        variant="soft"
                        icon="i-lucide-copy"
                        @click="copyFromFirstParticipant(index)"
                      >
                        {{ t('checkout.copy_from_first') }}
                      </UButton>
                    </div>
                  </template>
                </BookingParticipantForm>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                  @click="prevStep"
                >
                  {{ t('checkout.back') }}
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  :disabled="!step2Valid"
                  @click="nextStep"
                >
                  {{ t('checkout.continue') }}
                </UButton>
              </div>
            </template>
          </UCard>

          <!-- Step 3: Payment -->
          <UCard v-if="currentStep === 3">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t('payment.select_method') }}
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
                {{ t('payment.select_method_description') }}
              </p>
            </template>

            <PaymentMethodSelector
              v-model="selectedPaymentMethod"
            />

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                  @click="prevStep"
                >
                  {{ t('common.back') }}
                </UButton>
                <UButton
                  color="primary"
                  size="lg"
                  :icon="isSubmitting ? 'i-lucide-loader-2' : 'i-lucide-lock'"
                  :loading="isSubmitting"
                  :disabled="isSubmitting || !selectedPaymentMethod"
                  @click="submitBooking"
                >
                  {{ isSubmitting ? t('booking.mock_payment_processing') : `${t('common.confirm')} ${formatPrice(total)}` }}
                </UButton>
              </div>
            </template>
          </UCard>
        </div>

        <!-- Order Summary Sidebar -->
        <div class="lg:col-span-1">
          <div class="sticky top-4">
            <UCard>
              <template #header>
                <h3
                  class="text-lg font-semibold text-neutral-900 dark:text-white"
                >
                  {{ t('checkout.order_summary') }}
                </h3>
              </template>

              <div class="space-y-4">
                <!-- Cart Items -->
                <div
                  v-for="item in cartStore.cart.items"
                  :key="item.id || item.scheduleId"
                  class="pb-4 border-b border-neutral-200 dark:border-neutral-700"
                >
                  <p
                    class="font-medium text-neutral-900 dark:text-white text-sm"
                  >
                    {{ item.tourName }}
                  </p>
                  <p
                    class="text-xs text-neutral-500 dark:text-neutral-300 mt-1"
                  >
                    {{ item.numParticipants }}
                    {{ item.numParticipants === 1 ? t('checkout.person') : t('checkout.persons') }}
                  </p>
                  <p
                    class="text-sm font-semibold text-neutral-900 dark:text-white mt-2"
                  >
                    {{ formatPrice(item.itemTotal ?? 0) }}
                  </p>
                </div>

                <!-- Total -->
                <div class="space-y-2 pt-2">
                  <div
                    class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white"
                  >
                    <span>{{ t('checkout.total') }}</span>
                    <span>{{ formatPrice(total) }}</span>
                  </div>
                  <p class="text-xs text-neutral-500 dark:text-neutral-400 text-right">
                    {{ t('checkout.tax_included') }}
                  </p>
                </div>
              </div>
            </UCard>
          </div>
        </div>
      </div>

      <!-- PIX Payment Modal -->
      <UModal
        v-model:open="showPIXModal"
        :prevent-close="true"
      >
        <template #content>
          <div class="p-6">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-xl font-semibold text-neutral-900 dark:text-white">
                {{ t('payment.pix.scan_qr_code') }}
              </h3>
              <UButton
                icon="i-lucide-x"
                color="neutral"
                variant="ghost"
                size="sm"
                @click="showPIXModal = false"
              />
            </div>

            <PaymentPIXDisplay
              v-if="paymentStore.currentPayment"
              :payment="paymentStore.currentPayment"
              @success="handlePIXSuccess"
              @failed="handlePIXFailed"
              @expired="handlePIXExpired"
            />
          </div>
        </template>
      </UModal>
    </UContainer>
  </div>
</template>
