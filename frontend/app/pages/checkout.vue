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
const { countries } = useCountries()

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
const participants = ref<
  Array<{
    fullName: string
    documentId: string
    nationality: string
    dateOfBirth: string | null
    pickupAddress: string
    specialRequirements: string
    phoneNumber: string
    email: string
  }>
>([])

// Initialize participants based on total count
const totalParticipants = computed(() => cartStore.totalItems)

function initializeParticipants() {
  participants.value = Array.from(
    { length: totalParticipants.value },
    (_, i) => ({
      fullName: i === 0 ? contactForm.value.fullName : '',
      documentId: '',
      nationality: 'CL',
      dateOfBirth: null,
      pickupAddress: '',
      specialRequirements: '',
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
      && contactForm.value.phone.length >= 8

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
      title: 'Información incompleta',
      description: 'Por favor completa todos los campos requeridos'
    })
    return
  }

  if (currentStep.value === 1) {
    initializeParticipants()
  }

  if (currentStep.value === 2 && !step2Valid.value) {
    toast.add({
      color: 'warning',
      title: 'Información incompleta',
      description:
        'Por favor completa la información de todos los participantes'
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

// Clone contact info to first participant
function cloneContactToParticipant() {
  if (participants.value.length === 0) {
    initializeParticipants()
  }
  if (participants.value[0]) {
    participants.value[0].fullName = contactForm.value.fullName
    participants.value[0].email = contactForm.value.email
    participants.value[0].phoneNumber = contactForm.value.phone
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

// Submit booking
const isSubmitting = ref(false)
const createdBookingIds = ref<string[]>([])

async function submitBooking() {
  if (isSubmitting.value || !selectedPaymentMethod.value) return
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
      } catch (error: unknown) {
        const statusCode = error && typeof error === 'object' && 'statusCode' in error
          ? (error as { statusCode?: number }).statusCode
          : undefined

        if (statusCode === 409) {
          toast.add({
            color: 'warning',
            title: t('common.error'),
            description: 'Ya tienes una cuenta. Por favor inicia sesión.'
          })
          router.push(localePath('/auth'))
          return
        }
        throw error
      }
    }

    const token = authStore.token

    // Step 2: Create bookings for ALL cart items
    toast.add({
      color: 'info',
      title: 'Procesando reserva...',
      description: 'Estamos creando tu reserva. Este proceso puede tomar unos segundos.',
      icon: 'i-lucide-loader-2'
    })

    const cartItems = cartStore.cart.items
    if (!cartItems || cartItems.length === 0) {
      throw new Error('No items in cart')
    }

    // Create bookings for each cart item
    let participantOffset = 0
    const bookingResponses = []

    for (const item of cartItems) {
      const bookingParticipants = participants.value.slice(
        participantOffset,
        participantOffset + item.numParticipants
      )

      // Map to backend format
      const participantReqs = bookingParticipants.map(p => ({
        fullName: p.fullName,
        documentId: p.documentId,
        nationality: p.nationality,
        dateOfBirth: p.dateOfBirth || null,
        pickupAddress: p.pickupAddress || null,
        specialRequirements: p.specialRequirements || null,
        phoneNumber: p.phoneNumber || null,
        email: p.email || null
      }))

      // Create booking (will be PENDING)
      const bookingReq = {
        scheduleId: item.scheduleId,
        participants: participantReqs,
        languageCode: locale.value,
        specialRequests: null
      }

      const bookingRes = await $fetch<any>(`${config.public.apiBase}/api/bookings`, {
        method: 'POST',
        body: bookingReq,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      bookingResponses.push(bookingRes)
      createdBookingIds.value.push(bookingRes.id)
      participantOffset += item.numParticipants
    }

    // Step 3: Initialize payment for all bookings (using first booking as reference)
    toast.add({
      color: 'success',
      title: '✅ Reserva creada exitosamente',
      description: 'Preparando pasarela de pago segura...',
      icon: 'i-lucide-check-circle'
    })

    const primaryBooking = bookingResponses[0]
    const tourNames = cartItems.map(item => item.tourName).join(', ')

    // Collect additional booking IDs (all except the first one)
    const additionalBookingIds = bookingResponses.slice(1).map(b => b.id)

    const paymentResult = await paymentStore.initializePayment({
      bookingId: primaryBooking.id,
      additionalBookingIds: additionalBookingIds.length > 0 ? additionalBookingIds : undefined,
      provider: selectedPaymentMethod.value.provider,
      paymentMethod: selectedPaymentMethod.value.method,
      amount: total.value,
      currency: 'CLP',
      returnUrl: `${window.location.origin}/payment/callback?bookingId=${primaryBooking.id}`,
      cancelUrl: `${window.location.origin}/checkout`,
      userEmail: contactForm.value.email,
      description: `Reserva para ${tourNames}`
    })

    // Step 4: Handle payment flow based on method
    if (selectedPaymentMethod.value.method === PaymentMethod.WEBPAY) {
      // Transbank: Redirect to payment URL
      if (paymentResult.paymentUrl) {
        toast.add({
          color: 'success',
          title: '🔒 Redirigiendo a entorno seguro...',
          description: 'Te estamos llevando a la pasarela de pago de Transbank. No cierres esta ventana.',
          timeout: 0 // Keep toast until redirect
        })

        // Redirect to Transbank with a slight delay for better UX
        setTimeout(() => {
          window.location.href = paymentResult.paymentUrl!
        }, 1500)
      } else {
        throw new Error('No payment URL received from Transbank')
      }
    } else if (selectedPaymentMethod.value.method === PaymentMethod.PIX) {
      // PIX: Show QR code modal
      showPIXModal.value = true
    }
  } catch (error: unknown) {
    console.error('Error in checkout process:', error)

    // Rollback: Cancel ALL bookings if they were created
    if (createdBookingIds.value.length > 0) {
      for (const bookingId of createdBookingIds.value) {
        try {
          await $fetch(`${config.public.apiBase}/api/admin/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: {
              Authorization: `Bearer ${authStore.token}`
            }
          })
        } catch (e) {
          console.error(`Error rolling back booking ${bookingId}:`, e)
        }
      }
      createdBookingIds.value = []
    }

    let errorMessage = 'Hubo un error procesando tu reserva.'

    // Handle specific errors
    if (error.data?.message) {
      if (error.data.message.includes('Not enough available slots')) {
        errorMessage = 'No hay suficientes cupos disponibles para esta fecha.'
      } else if (error.data.message.includes('not found')) {
        errorMessage = 'El tour seleccionado ya no está disponible.'
      } else {
        errorMessage = error.data.message
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

// Format currency
function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP'
  }).format(amount)
}

// Calculate totals
const subtotal = computed(() => cartStore.cart.cartTotal)
const tax = computed(() => subtotal.value * 0.19)
const total = computed(() => subtotal.value + tax.value)
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-8">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          Finalizar Compra
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300">
          Completa tu reserva en {{ totalSteps }} simples pasos
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
                Contacto
              </span>
              <span
                :class="
                  currentStep >= 2
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-300'
                "
              >
                Participantes
              </span>
              <span
                :class="
                  currentStep >= 3
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-300'
                "
              >
                Pago
              </span>
            </div>
          </div>

          <!-- Step 1: Contact Information -->
          <UCard v-if="currentStep === 1">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                Información de Contacto
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
                Usaremos esta información para enviarte la confirmación
              </p>
            </template>

            <div class="space-y-4">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                >
                  Nombre Completo *
                </label>
                <input
                  v-model="contactForm.fullName"
                  type="text"
                  placeholder="Juan Pérez"
                  class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  required
                >
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                >
                  Email *
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
                  Teléfono *
                </label>
                <div class="flex gap-2">
                  <select
                    v-model="contactForm.countryCode"
                    class="w-24 px-3 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  >
                    <option value="+56">
                      +56
                    </option>
                    <option value="+55">
                      +55
                    </option>
                    <option value="+1">
                      +1
                    </option>
                    <option value="+44">
                      +44
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
                  Crea una cuenta para gestionar tus reservas fácilmente. Si ya tienes una, <NuxtLink
                    :to="{ path: localePath('/auth'), query: { redirect: localePath('/checkout') } }"
                    class="text-primary font-medium hover:underline"
                  >inicia sesión aquí</NuxtLink>.
                </p>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-1"
                    >
                      Contraseña *
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
                      Confirmar Contraseña *
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
                  Volver al Carrito
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  :disabled="!step1Valid"
                  @click="nextStep"
                >
                  Continuar
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
                Datos de Participantes
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
                {{ totalParticipants }}
                {{ totalParticipants === 1 ? "persona" : "personas" }} en total
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
                        Participante {{ index + 1 }}
                        <span
                          v-if="index === 0"
                          class="text-sm text-neutral-500 dark:text-neutral-300 font-normal ml-2"
                        >
                          (Contacto principal)
                        </span>
                      </h3>
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
                  Atrás
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  :disabled="!step2Valid"
                  @click="nextStep"
                >
                  Continuar
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
              :amount="total"
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
                  {{ isSubmitting ? t('booking.mock_payment_processing') : `${t('common.confirm')} ${formatCurrency(total)}` }}
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
                  Resumen de Compra
                </h3>
              </template>

              <div class="space-y-4">
                <!-- Cart Items -->
                <div
                  v-for="item in cartStore.cart.items"
                  :key="item.itemId"
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
                    {{ item.numParticipants === 1 ? "persona" : "personas" }}
                  </p>
                  <p
                    class="text-sm font-semibold text-neutral-900 dark:text-white mt-2"
                  >
                    {{ formatCurrency(item.itemTotal) }}
                  </p>
                </div>

                <!-- Totals -->
                <div class="space-y-2 pt-2">
                  <div
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-300"
                  >
                    <span>Subtotal</span>
                    <span>{{ formatCurrency(subtotal) }}</span>
                  </div>
                  <div
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-300"
                  >
                    <span>IVA (19%)</span>
                    <span>{{ formatCurrency(tax) }}</span>
                  </div>
                  <UDivider />
                  <div
                    class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white"
                  >
                    <span>Total</span>
                    <span>{{ formatCurrency(total) }}</span>
                  </div>
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
