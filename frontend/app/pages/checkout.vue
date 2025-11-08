<script setup lang="ts">
const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()
const toast = useToast()
const { locale } = useI18n()
const { countries } = useCountries()

// Redirect if cart is empty
if (cartStore.cart.items.length === 0) {
  router.push('/cart')
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
const paymentMethod = ref('credit_card')

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
  if (participants.value.length > 0) {
    participants.value[0].fullName = contactForm.value.fullName
    participants.value[0].email = contactForm.value.email
    participants.value[0].phoneNumber = contactForm.value.phone
  }
}

// Update participant data
function updateParticipant(index: number, data: Partial<typeof participants.value[0]>) {
  participants.value[index] = {
    ...participants.value[index],
    ...data
  }
}

// Submit booking
const isSubmitting = ref(false)

async function submitBooking() {
  if (isSubmitting.value) return
  isSubmitting.value = true

  const config = useRuntimeConfig()
  const createdBookingIds: string[] = []

  try {
    // Step 1: Ensure user is authenticated
    if (!authStore.isAuthenticated) {
      toast.add({
        color: 'info',
        title: 'Creando cuenta...',
        description: 'Estamos registrando tu información'
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
      } catch (error: any) {
        if (error.statusCode === 409) {
          toast.add({
            color: 'warning',
            title: 'Cuenta existente',
            description: 'Ya tienes una cuenta. Por favor inicia sesión.'
          })
          router.push('/auth')
          return
        }
        throw error
      }
    }

    const token = authStore.token

    // Step 2: Create all bookings (status: PENDING)
    toast.add({
      color: 'info',
      title: 'Procesando reservas...',
      description: 'Creando tus reservas'
    })

    let participantIndex = 0

    for (const item of cartStore.cart.items) {
      // Assign participants to this booking
      const numParticipantsForThisItem = item.numParticipants
      const bookingParticipants = participants.value.slice(
        participantIndex,
        participantIndex + numParticipantsForThisItem
      )
      participantIndex += numParticipantsForThisItem

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

      try {
        const bookingRes = await $fetch<any>(`${config.public.apiBase}/api/bookings`, {
          method: 'POST',
          body: bookingReq,
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })

        createdBookingIds.push(bookingRes.id)
      } catch (error: any) {
        // If booking creation fails, rollback by cancelling already created bookings
        console.error('Error creating booking:', error)

        // Rollback: Cancel all previously created bookings
        for (const bookingId of createdBookingIds) {
          try {
            await $fetch(`${config.public.apiBase}/api/admin/bookings/${bookingId}`, {
              method: 'DELETE',
              headers: {
                Authorization: `Bearer ${token}`
              }
            })
          } catch (e) {
            console.error('Error rolling back booking:', e)
          }
        }

        throw error
      }
    }

    // Step 3: Simulate payment processing
    toast.add({
      color: 'info',
      title: 'Procesando pago...',
      description: 'Simulando pasarela de pago'
    })

    // Wait 1.5 seconds to simulate payment gateway
    await new Promise(resolve => setTimeout(resolve, 1500))

    // Step 4: Confirm all bookings (PENDING -> CONFIRMED)
    toast.add({
      color: 'info',
      title: 'Confirmando reservas...',
      description: 'Finalizando tu compra'
    })

    for (const bookingId of createdBookingIds) {
      try {
        await $fetch(`${config.public.apiBase}/api/bookings/${bookingId}/confirm-mock`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`
          }
        })
      } catch (error: any) {
        console.error('Error confirming booking:', error)

        // If confirmation fails, the bookings remain PENDING
        // Admin can manually review them
        toast.add({
          color: 'warning',
          title: 'Advertencia',
          description: 'Algunas reservas quedaron pendientes de confirmación. Contacta a soporte.'
        })
      }
    }

    // Step 5: Clear cart (silently)
    const itemsToRemove = [...cartStore.cart.items]
    for (const item of itemsToRemove) {
      try {
        await $fetch(`${config.public.apiBase}/api/cart/items/${item.itemId}`, {
          method: 'DELETE',
          credentials: 'include'
        })
      } catch (e) {
        // Cart cleanup is not critical, ignore errors
        console.warn('Could not remove cart item:', e)
      }
    }

    // Clear local cart state
    cartStore.clearCart()

    // Success!
    toast.add({
      color: 'success',
      title: '¡Reservas confirmadas!',
      description: `Se confirmaron ${createdBookingIds.length} reserva(s) exitosamente`
    })

    // Redirect to bookings page
    router.push('/profile/bookings')
  } catch (error: any) {
    console.error('Error in checkout process:', error)

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
      title: 'Error al crear reserva',
      description: errorMessage
    })
  } finally {
    isSubmitting.value = false
  }
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
        <p class="text-neutral-600 dark:text-neutral-400">
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
                      : 'bg-neutral-200 dark:bg-neutral-700 text-neutral-600 dark:text-neutral-400'
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
                    : 'text-neutral-500 dark:text-neutral-400'
                "
              >
                Contacto
              </span>
              <span
                :class="
                  currentStep >= 2
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-400'
                "
              >
                Participantes
              </span>
              <span
                :class="
                  currentStep >= 3
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-400'
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
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                Usaremos esta información para enviarte la confirmación
              </p>
            </template>

            <div class="space-y-4">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
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
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
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
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
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
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
                  Crea una cuenta para gestionar tus reservas fácilmente. Si ya tienes una, <NuxtLink
                    to="/auth?redirect=/checkout"
                    class="text-primary font-medium hover:underline"
                  >inicia sesión aquí</NuxtLink>.
                </p>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
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
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
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
                  to="/cart"
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
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
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
                          class="text-sm text-neutral-500 dark:text-neutral-400 font-normal ml-2"
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
                Método de Pago
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                Selecciona cómo deseas pagar
              </p>
            </template>

            <div class="space-y-4">
              <div
                class="p-4 rounded-lg border cursor-pointer transition-colors"
                :class="[
                  paymentMethod === 'credit_card'
                    ? 'border-primary bg-primary/10'
                    : 'border-neutral-200 dark:border-neutral-700 hover:border-primary/50'
                ]"
                @click="paymentMethod = 'credit_card'"
              >
                <div class="flex items-center gap-3">
                  <UIcon
                    name="i-lucide-credit-card"
                    class="w-6 h-6 text-primary"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Tarjeta de Crédito/Débito
                    </p>
                    <p class="text-sm text-neutral-500 dark:text-neutral-400">
                      Visa, Mastercard, AmEx
                    </p>
                  </div>
                </div>
              </div>

              <div
                class="p-4 rounded-lg border cursor-pointer transition-colors"
                :class="[
                  paymentMethod === 'transfer'
                    ? 'border-primary bg-primary/10'
                    : 'border-neutral-200 dark:border-neutral-700 hover:border-primary/50'
                ]"
                @click="paymentMethod = 'transfer'"
              >
                <div class="flex items-center gap-3">
                  <UIcon
                    name="i-lucide-building-2"
                    class="w-6 h-6 text-primary"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Transferencia Bancaria
                    </p>
                    <p class="text-sm text-neutral-500 dark:text-neutral-400">
                      Pago directo desde tu banco
                    </p>
                  </div>
                </div>
              </div>

              <div
                class="mt-6 p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg"
              >
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
                  <UIcon
                    name="i-lucide-info"
                    class="w-4 h-4 inline mr-1"
                  />
                  Este es un pago de demostración. No se procesará ningún cargo
                  real.
                </p>
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
                  size="lg"
                  :icon="isSubmitting ? 'i-lucide-loader-2' : 'i-lucide-lock'"
                  :loading="isSubmitting"
                  :disabled="isSubmitting"
                  @click="submitBooking"
                >
                  {{ isSubmitting ? 'Procesando...' : `Pagar ${formatCurrency(total)}` }}
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
                    class="text-xs text-neutral-500 dark:text-neutral-400 mt-1"
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
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400"
                  >
                    <span>Subtotal</span>
                    <span>{{ formatCurrency(subtotal) }}</span>
                  </div>
                  <div
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400"
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
    </UContainer>
  </div>
</template>
