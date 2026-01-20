<script setup lang="ts">
import type { PaymentSessionRes } from 'api-client'
import { useDebounceFn } from '@vueuse/core'
import { PaymentProvider, PaymentMethod } from '~/types/payment'
import { logger } from '~/utils/logger'

const cartStore = useCartStore()
const authStore = useAuthStore()
const paymentStore = usePaymentStore()
const router = useRouter()
const toast = useToast()
const { locale, t } = useI18n()
const localePath = useLocalePath()
const { phoneCodes, getCountryFlag } = useCountries()
const { formatPrice } = useCurrency()
const { participants: savedParticipants, fetchParticipants: fetchSavedParticipants } = useSavedParticipants()

// Auth mode for Step 1 (register vs login)
const authMode = ref<'register' | 'login'>('register')
const emailCheckResult = ref<{ exists: boolean } | null>(null)
const isCheckingEmail = ref(false)

// Cart conflict state
const showCartConflictModal = ref(false)

// UTabs items for auth mode toggle
const authModeItems = computed(() => [
  { label: t('auth.create_account'), value: 'register' },
  { label: t('auth.have_account'), value: 'login' }
])

// Email validation helper
function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// Debounced email check (1 second delay)
const debouncedCheckEmail = useDebounceFn(async (email: string) => {
  if (!email || !isValidEmail(email)) {
    emailCheckResult.value = null
    return
  }

  isCheckingEmail.value = true
  try {
    emailCheckResult.value = await $fetch<{ exists: boolean }>('/api/auth/check-email', {
      params: { email }
    })
  } catch (error) {
    logger.error('Error checking email:', error)
    emailCheckResult.value = null
  } finally {
    isCheckingEmail.value = false
  }
}, 1000)

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
  savedParticipantId?: string
  markAsSelf?: boolean
  saveForFuture?: boolean
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

// Watch email changes for verification (must be after contactForm definition)
watch(() => contactForm.value.email, (email) => {
  // Only check if user is not authenticated
  if (!authStore.isAuthenticated && email) {
    debouncedCheckEmail(email)
  }
})

// Step 2: Participants
const participants = ref<Participant[]>([])

// LocalStorage keys for checkout persistence
const CHECKOUT_CONTACT_KEY = 'checkout_contact'
const CHECKOUT_PARTICIPANTS_KEY = 'checkout_participants'
const CHECKOUT_STEP_KEY = 'checkout_step'
const CHECKOUT_TIMESTAMP_KEY = 'checkout_timestamp'

// Checkout data expires after 24 hours (in milliseconds)
const CHECKOUT_EXPIRY_MS = 24 * 60 * 60 * 1000

/**
 * Check if checkout data has expired (older than 24 hours)
 */
function isCheckoutDataExpired(): boolean {
  if (!import.meta.client) return true
  try {
    const timestamp = localStorage.getItem(CHECKOUT_TIMESTAMP_KEY)
    if (!timestamp) return true
    const savedTime = parseInt(timestamp, 10)
    if (isNaN(savedTime)) return true
    return Date.now() - savedTime > CHECKOUT_EXPIRY_MS
  } catch {
    return true
  }
}

/**
 * Update the checkout timestamp
 */
function updateCheckoutTimestamp(): void {
  if (import.meta.client) {
    try {
      localStorage.setItem(CHECKOUT_TIMESTAMP_KEY, String(Date.now()))
    } catch {
      // Ignore storage errors
    }
  }
}

// Load saved checkout data on mount
onMounted(async () => {
  // Fetch cart from backend to ensure we have latest data
  await cartStore.fetchCart()

  // Fetch saved participants if authenticated
  if (authStore.isAuthenticated) {
    await fetchSavedParticipants()
  }

  try {
    // Check if saved data has expired (older than 24 hours)
    if (isCheckoutDataExpired()) {
      clearCheckoutData()
    } else {
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
    }
  } catch (e) {
    logger.error('Error loading checkout data from localStorage:', e)
  }
})

// Computed for localStorage save (excluding passwords)
const contactFormForSave = computed(() => JSON.stringify({
  email: contactForm.value.email,
  fullName: contactForm.value.fullName,
  phone: contactForm.value.phone,
  countryCode: contactForm.value.countryCode
}))

// Save contact form when it changes (watch primitive string instead of deep object)
watch(contactFormForSave, (json) => {
  if (import.meta.client) {
    localStorage.setItem(CHECKOUT_CONTACT_KEY, json)
    updateCheckoutTimestamp()
  }
})

// Serialize participants for efficient watching
const participantsForSave = computed(() =>
  participants.value.length > 0 ? JSON.stringify(participants.value) : ''
)

// Save participants when they change (watch primitive string instead of deep array)
watch(participantsForSave, (json) => {
  if (import.meta.client && json) {
    localStorage.setItem(CHECKOUT_PARTICIPANTS_KEY, json)
    updateCheckoutTimestamp()
  }
})

// Save current step
watch(currentStep, (newVal) => {
  if (import.meta.client) {
    localStorage.setItem(CHECKOUT_STEP_KEY, String(newVal))
    updateCheckoutTimestamp()
  }
})

// Clear checkout data from localStorage (call after successful payment)
function clearCheckoutData() {
  if (import.meta.client) {
    localStorage.removeItem(CHECKOUT_CONTACT_KEY)
    localStorage.removeItem(CHECKOUT_PARTICIPANTS_KEY)
    localStorage.removeItem(CHECKOUT_STEP_KEY)
    localStorage.removeItem(CHECKOUT_TIMESTAMP_KEY)
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

  // For non-authenticated users, validate based on auth mode
  if (authMode.value === 'register') {
    // Register: need password + confirm password
    const passwordValidation
      = contactForm.value.password.length >= 8
        && contactForm.value.password === contactForm.value.confirmPassword
    return baseValidation && passwordValidation
  } else {
    // Login: only need password (no confirm)
    return baseValidation && contactForm.value.password.length >= 1
  }
})

const step2Valid = computed(() => {
  return participants.value.every(
    p => p.fullName && p.documentId && p.nationality
  )
})

// Navigation
const isProcessingAuth = ref(false)

async function nextStep() {
  if (currentStep.value === 1 && !step1Valid.value) {
    toast.add({
      color: 'warning',
      title: t('checkout.validation.incomplete_info'),
      description: t('checkout.validation.complete_required_fields')
    })
    return
  }

  if (currentStep.value === 1) {
    // Handle authentication before advancing to Step 2
    if (!authStore.isAuthenticated) {
      isProcessingAuth.value = true
      try {
        if (authMode.value === 'register') {
          // Register new user
          toast.add({
            color: 'info',
            title: t('auth.creating_account'),
            icon: 'i-lucide-loader-2'
          })

          await authStore.register({
            email: contactForm.value.email,
            password: contactForm.value.password,
            fullName: contactForm.value.fullName,
            phoneNumber: contactForm.value.phone ? `${contactForm.value.countryCode}${contactForm.value.phone}` : null,
            nationality: null
          })

          // Auto-login after registration
          await authStore.login({
            email: contactForm.value.email,
            password: contactForm.value.password
          })

          if (!authStore.isAuthenticated) {
            throw new Error('Login failed after registration')
          }

          toast.add({
            color: 'success',
            title: t('auth.welcome')
          })
        } else {
          // Login existing user
          toast.add({
            color: 'info',
            title: t('auth.logging_in'),
            icon: 'i-lucide-loader-2'
          })

          // Save current cart before login for conflict detection
          cartStore.savePreLoginCart()

          await authStore.login({
            email: contactForm.value.email,
            password: contactForm.value.password
          })

          if (!authStore.isAuthenticated) {
            throw new Error('Login failed')
          }

          // Fetch the user's cart from backend (might differ from pre-login cart)
          await cartStore.fetchCart()

          // Check for cart conflict
          if (cartStore.hasCartConflict()) {
            showCartConflictModal.value = true
            isProcessingAuth.value = false
            // Don't advance - wait for user to resolve conflict
            return
          }

          toast.add({
            color: 'success',
            title: t('auth.welcome')
          })
        }

        // Fetch saved participants now that user is authenticated
        await fetchSavedParticipants()
      } catch (error: unknown) {
        const statusCode = error && typeof error === 'object' && 'statusCode' in error
          ? (error as { statusCode?: number }).statusCode
          : undefined

        if (statusCode === 409) {
          // Email already exists (register mode)
          toast.add({
            color: 'warning',
            title: t('common.error'),
            description: t('checkout.toast.account_exists')
          })
          authMode.value = 'login'
        } else if (statusCode === 401) {
          // Invalid credentials (login mode)
          toast.add({
            color: 'error',
            title: t('common.error'),
            description: t('auth.invalid_credentials')
          })
        } else {
          toast.add({
            color: 'error',
            title: t('common.error'),
            description: t('common.error_unknown')
          })
        }
        isProcessingAuth.value = false
        return
      }
      isProcessingAuth.value = false
    }

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
function updateParticipant(index: number, data: Partial<Participant>) {
  const current = participants.value[index]
  if (!current) return

  // If marking this participant as self, unmark any other participant that was marked as self
  if (data.markAsSelf === true) {
    participants.value.forEach((p, i) => {
      if (i !== index && p.markAsSelf) {
        p.markAsSelf = false
      }
    })
  }

  participants.value[index] = {
    ...current,
    ...data
  }
}

// Check if another participant slot has markAsSelf
function hasSelfInOtherSlot(currentIndex: number): boolean {
  return participants.value.some((p, i) => i !== currentIndex && p.markAsSelf === true)
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

// Auto-copy pickup address from first participant to all others when first participant changes
watch(() => participants.value[0]?.pickupAddress, (newPickupAddress) => {
  if (!newPickupAddress || participants.value.length <= 1) return

  // Copy to all other participants that have empty pickup address
  for (let i = 1; i < participants.value.length; i++) {
    const p = participants.value[i]
    if (p && !p.pickupAddress) {
      p.pickupAddress = newPickupAddress
    }
  }
}, { immediate: false })

// Submit payment - creates PaymentSession, then redirects to payment provider
const isSubmitting = ref(false)
const lastSubmitTime = ref(0)

async function submitBooking() {
  // Prevent double submit - strict check
  if (isSubmitting.value) {
    logger.warn('Submit already in progress, ignoring')
    return
  }

  // Prevent rapid double-click
  const now = Date.now()
  if (now - lastSubmitTime.value < 3000) {
    logger.warn('Submit too fast, ignoring')
    return
  }
  lastSubmitTime.value = now

  if (!selectedPaymentMethod.value) return
  isSubmitting.value = true

  const config = useRuntimeConfig()

  try {
    // Step 1: Verify user is authenticated (should already be done in Step 1 -> Step 2 transition)
    if (!authStore.isAuthenticated) {
      toast.add({
        color: 'error',
        title: t('common.error'),
        description: t('auth.invalid_credentials')
      })
      currentStep.value = 1
      return
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
        itemTotal: item.itemTotal,
        specialRequests: null,
        participants: itemParticipants.map(p => ({
          fullName: p.fullName,
          documentId: p.documentId,
          nationality: p.nationality || null,
          dateOfBirth: p.dateOfBirth || null,
          pickupAddress: p.pickupAddress || null,
          specialRequirements: p.specialRequirements || null,
          phoneNumber: p.phoneNumber ? `${p.phoneCountryCode}${p.phoneNumber}` : null,
          email: p.email || null,
          savedParticipantId: p.savedParticipantId || null,
          markAsSelf: p.markAsSelf || false,
          saveForFuture: p.saveForFuture || false
        }))
      }
    })

    const tourNames = cartItems.map(item => item.tourName).join(', ')

    // Step 3: Create PaymentSession (no bookings created yet!)
    // Transbank uses POST callback, MercadoPago uses GET redirect
    const isTransbank = selectedPaymentMethod.value.provider === PaymentProvider.Transbank
    const returnUrl = isTransbank
      ? `${config.public.baseUrl}/api/payments/callback`
      : `${config.public.baseUrl}/payment/callback`

    const paymentSessionRes = await $fetch<PaymentSessionRes>('/api/payment-sessions', {
      method: 'POST',
      body: {
        provider: selectedPaymentMethod.value.provider,
        paymentMethod: selectedPaymentMethod.value.method,
        totalAmount: cartTotal,
        currency: 'CLP',
        items: sessionItems,
        returnUrl,
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
        sessionId: paymentSessionRes.sessionId,
        status: paymentSessionRes.status,
        qrCode: paymentSessionRes.qrCode,
        pixCode: paymentSessionRes.pixCode,
        expiresAt: paymentSessionRes.expiresAt
      })
    }

    // Step 4: Handle payment flow based on method
    if (selectedPaymentMethod.value.method === PaymentMethod.Webpay) {
      // Transbank Webpay - POST redirect to Transbank
      if (paymentSessionRes.paymentUrl && paymentSessionRes.token) {
        toast.add({
          color: 'success',
          title: t('checkout.toast.redirecting_payment'),
          description: t('checkout.toast.redirecting_provider', { provider: 'Transbank' })
        })

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
    } else if (selectedPaymentMethod.value.method === PaymentMethod.CreditCard) {
      // MercadoPago Checkout Pro - GET redirect
      if (paymentSessionRes.paymentUrl) {
        toast.add({
          color: 'success',
          title: t('checkout.toast.redirecting_payment'),
          description: t('checkout.toast.redirecting_provider', { provider: 'MercadoPago' })
        })

        // MercadoPago uses simple redirect
        setTimeout(() => {
          window.location.href = paymentSessionRes.paymentUrl!
        }, 1500)
      } else {
        throw new Error('No payment URL received from MercadoPago')
      }
    } else if (selectedPaymentMethod.value.method === PaymentMethod.Pix) {
      // QR Code: Show QR code modal
      showPIXModal.value = true
    }
  } catch (error: unknown) {
    logger.error('Error in checkout process:', error)

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

// Handle cart conflict resolution
async function handleCartConflictSelect(choice: 'current' | 'saved' | 'merge') {
  showCartConflictModal.value = false

  await cartStore.resolveCartConflict(choice)

  toast.add({
    color: 'success',
    title: t('auth.welcome')
  })

  // Fetch saved participants now that user is authenticated
  await fetchSavedParticipants()

  // Initialize participants and advance to step 2
  initializeParticipants()
  currentStep.value = 2
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
              <!-- Auth Mode Toggle (only if not authenticated) -->
              <div
                v-if="!authStore.isAuthenticated"
                class="mb-6"
              >
                <UTabs
                  v-model="authMode"
                  :items="authModeItems"
                  class="w-full"
                />
              </div>

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
                <div class="relative">
                  <input
                    v-model="contactForm.email"
                    type="email"
                    placeholder="juan@example.com"
                    class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  >
                  <!-- Loading indicator for email check -->
                  <div
                    v-if="isCheckingEmail"
                    class="absolute right-3 top-1/2 -translate-y-1/2"
                  >
                    <UIcon
                      name="i-lucide-loader-2"
                      class="w-4 h-4 text-neutral-400 animate-spin"
                    />
                  </div>
                </div>

                <!-- Email verification messages -->
                <p
                  v-if="!authStore.isAuthenticated && emailCheckResult?.exists && authMode === 'register'"
                  class="text-warning-500 text-sm mt-1"
                >
                  {{ t('checkout.email_exists') }}
                  <button
                    type="button"
                    class="underline font-medium ml-1"
                    @click="authMode = 'login'"
                  >
                    {{ t('auth.login_instead') }}
                  </button>
                </p>
                <p
                  v-if="!authStore.isAuthenticated && emailCheckResult && !emailCheckResult.exists && authMode === 'login'"
                  class="text-warning-500 text-sm mt-1"
                >
                  {{ t('checkout.email_not_found') }}
                  <button
                    type="button"
                    class="underline font-medium ml-1"
                    @click="authMode = 'register'"
                  >
                    {{ t('auth.register_instead') }}
                  </button>
                </p>
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

              <!-- Password fields (only if not authenticated) -->
              <div
                v-if="!authStore.isAuthenticated"
                class="space-y-4 mt-4"
              >
                <!-- Register mode: password + confirm -->
                <template v-if="authMode === 'register'">
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
                      <p class="text-xs text-neutral-500 mt-1">
                        {{ t('auth.password_min') }}
                      </p>
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
                </template>

                <!-- Login mode: password only -->
                <template v-else>
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
                    <NuxtLink
                      :to="localePath('/auth/forgot-password')"
                      class="text-xs text-primary hover:underline mt-1 inline-block"
                    >
                      {{ t('auth.forgot_password') }}
                    </NuxtLink>
                  </div>
                </template>
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
                  :icon="isProcessingAuth ? 'i-lucide-loader-2' : 'i-lucide-arrow-right'"
                  trailing
                  :disabled="!step1Valid || isProcessingAuth"
                  :loading="isProcessingAuth"
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
                  :saved-participants="savedParticipants"
                  :has-self-participant-in-other-slot="hasSelfInOtherSlot(index)"
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
                  :key="item.itemId || item.scheduleId"
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
                    {{ formatPrice(item.itemTotal) }}
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
        class="max-w-2xl"
      >
        <template #content>
          <div class="max-h-[85vh] overflow-y-auto p-6">
            <PaymentPIXDisplay
              v-if="paymentStore.currentPayment"
              :payment="paymentStore.currentPayment"
              @success="handlePIXSuccess"
              @failed="handlePIXFailed"
              @expired="handlePIXExpired"
              @close="showPIXModal = false"
            />
          </div>
        </template>
      </UModal>

      <!-- Cart Conflict Modal -->
      <CheckoutCartConflictModal
        :open="showCartConflictModal"
        :current-cart="cartStore.cart.items"
        :saved-cart="cartStore.getPreLoginCart()"
        @select="handleCartConflictSelect"
        @close="showCartConflictModal = false"
      />
    </UContainer>
  </div>
</template>
