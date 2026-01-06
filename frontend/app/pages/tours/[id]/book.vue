<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-800 py-12">
    <UContainer>
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t("booking.title") }}
        </h1>
        <p
          v-if="tour"
          class="text-neutral-600 dark:text-neutral-300"
        >
          {{ getTourName(tour) }}
        </p>
      </div>

      <!-- Progress Steps -->
      <div class="mb-12">
        <div class="flex items-center justify-between max-w-3xl mx-auto">
          <StepIndicator
            v-for="(step, index) in steps"
            :key="index"
            :step="index + 1"
            :title="step.title"
            :active="currentStep === index"
            :completed="currentStep > index"
            :is-last="index === steps.length - 1"
          />
        </div>
      </div>

      <!-- Step Content -->
      <UCard class="max-w-4xl mx-auto">
        <template #content>
          <!-- Step 1: Select Schedule -->
          <div
            v-if="currentStep === 0"
            class="space-y-6"
          >
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t("booking.select_date") }}
            </h2>

            <div
              v-if="schedules && schedules.length > 0"
              class="grid gap-4"
            >
              <UButton
                v-for="schedule in schedules"
                :key="schedule.id"
                :variant="
                  bookingState.scheduleId === schedule.id ? 'solid' : 'outline'
                "
                :color="
                  bookingState.scheduleId === schedule.id
                    ? 'primary'
                    : 'neutral'
                "
                size="lg"
                block
                class="justify-between"
                @click="selectSchedule(schedule)"
              >
                <div class="flex items-center gap-3">
                  <UIcon
                    name="i-lucide-calendar"
                    class="w-5 h-5"
                  />
                  <span>{{ formatScheduleDate(schedule.startDatetime) }}</span>
                </div>
                <UBadge
                  v-if="schedule.availableSpots"
                  :label="`${schedule.availableSpots} disponibles`"
                  color="success"
                />
              </UButton>
            </div>
            <div
              v-else
              class="text-center py-8 text-neutral-600 dark:text-neutral-300"
            >
              {{ t("common.loading") }}
            </div>
          </div>

          <!-- Step 2: Select Participants -->
          <div
            v-else-if="currentStep === 1"
            class="space-y-6"
          >
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t("booking.select_participants") }}
            </h2>

            <div class="grid md:grid-cols-2 gap-6">
              <UFormField
                :label="t('booking.total_participants')"
                name="totalParticipants"
              >
                <div class="flex items-center gap-4">
                  <UButton
                    icon="i-lucide-minus"
                    color="neutral"
                    variant="outline"
                    size="lg"
                    :disabled="bookingState.totalParticipants <= 1"
                    @click="bookingState.totalParticipants--"
                  />
                  <span class="text-2xl font-semibold w-12 text-center">
                    {{ bookingState.totalParticipants }}
                  </span>
                  <UButton
                    icon="i-lucide-plus"
                    color="neutral"
                    variant="outline"
                    size="lg"
                    @click="bookingState.totalParticipants++"
                  />
                </div>
              </UFormField>
            </div>

            <!-- Price Summary -->
            <UCard v-if="tour">
              <div class="space-y-3">
                <div
                  class="flex justify-between text-neutral-700 dark:text-neutral-200"
                >
                  <span>{{ bookingState.totalParticipants }} x
                    {{ t("booking.participant") }}</span>
                  <span>${{
                    formatCurrency(
                      (tour?.price || 0) * bookingState.totalParticipants
                    )
                  }}</span>
                </div>
                <USeparator />
                <div
                  class="flex justify-between text-lg font-semibold text-neutral-900 dark:text-white"
                >
                  <span>{{ t("common.total") }}</span>
                  <span>${{ calculateTotal() }}</span>
                </div>
              </div>
            </UCard>
          </div>

          <!-- Step 3: Participant Details -->
          <div
            v-else-if="currentStep === 2"
            class="space-y-6"
          >
            <div class="flex justify-between items-center">
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t("booking.participant_details") }}
              </h2>
              <UButton
                v-if="authStore.user"
                variant="outline"
                color="primary"
                size="sm"
                icon="i-lucide-copy"
                @click="copyUserDataToFirstParticipant"
              >
                {{ t("booking.copy_user_data") }}
              </UButton>
            </div>

            <ParticipantForm
              v-for="(participant, index) in bookingState.participants"
              :key="index"
              :participant="participant"
              :index="index"
              :total-participants="bookingState.participants.length"
              @update="updateParticipant(index, $event)"
            />
          </div>

          <!-- Step 4: Payment -->
          <div
            v-else-if="currentStep === 3"
            class="space-y-6"
          >
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t("booking.mock_payment_title") }}
            </h2>

            <UAlert
              color="info"
              icon="i-lucide-info"
              :title="t('booking.mock_payment_title')"
              :description="t('booking.mock_payment_description')"
            />

            <!-- Price Summary -->
            <UCard v-if="tour">
              <div class="space-y-3">
                <div
                  class="flex justify-between text-neutral-700 dark:text-neutral-200"
                >
                  <span>{{ bookingState.totalParticipants }} x
                    {{ t("booking.participant") }}</span>
                  <span>${{
                    formatCurrency(
                      (tour?.price || 0) * bookingState.totalParticipants
                    )
                  }}</span>
                </div>
                <USeparator />
                <div
                  class="flex justify-between text-lg font-semibold text-neutral-900 dark:text-white"
                >
                  <span>{{ t("common.total") }}</span>
                  <span>${{ calculateTotal() }}</span>
                </div>
              </div>
            </UCard>

            <UButton
              color="primary"
              size="xl"
              block
              :loading="isProcessingPayment"
              :disabled="isProcessingPayment"
              @click="handleMockPayment"
            >
              {{
                isProcessingPayment
                  ? t("booking.mock_payment_processing")
                  : t("booking.mock_payment_button")
              }}
            </UButton>
          </div>

          <!-- Step 5: Confirmation -->
          <div
            v-else-if="currentStep === 4"
            class="space-y-6 text-center"
          >
            <div
              class="w-16 h-16 mx-auto bg-success/10 rounded-full flex items-center justify-center"
            >
              <UIcon
                name="i-lucide-check-circle"
                class="w-10 h-10 text-success"
              />
            </div>
            <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white">
              {{ t("booking.confirmation") }}
            </h2>
            <p class="text-neutral-600 dark:text-neutral-300">
              {{ t("booking.confirmation_message") }}
            </p>
            <div
              v-if="confirmedBooking"
              class="mt-6 p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg"
            >
              <div class="text-sm text-neutral-600 dark:text-neutral-300 mb-2">
                {{ t("booking.booking_reference") }}
              </div>
              <div
                class="text-lg font-mono font-semibold text-neutral-900 dark:text-white"
              >
                {{ confirmedBooking.id }}
              </div>
            </div>
            <div class="flex gap-4 justify-center mt-8">
              <UButton
                color="primary"
                size="lg"
                icon="i-lucide-calendar-check"
                :to="localePath('/profile/bookings')"
              >
                {{ t("booking.view_booking") }}
              </UButton>
              <UButton
                variant="outline"
                color="neutral"
                size="lg"
                icon="i-lucide-arrow-left"
                :to="localePath('/tours')"
              >
                {{ t("booking.book_another") }}
              </UButton>
            </div>
          </div>
        </template>
      </UCard>

      <!-- Navigation Buttons -->
      <div class="flex justify-between max-w-4xl mx-auto mt-8">
        <UButton
          v-if="currentStep > 0 && currentStep < 4"
          variant="outline"
          color="neutral"
          size="lg"
          icon="i-lucide-arrow-left"
          @click="previousStep"
        >
          {{ t("common.back") }}
        </UButton>
        <div v-else />

        <UButton
          v-if="currentStep < steps.length - 1 && currentStep !== 3"
          color="primary"
          size="lg"
          :disabled="!canProceed"
          @click="nextStep"
        >
          {{ t("common.next") }}
          <UIcon
            name="i-lucide-arrow-right"
            class="ml-2"
          />
        </UButton>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
// Fetch tour data
import type {
  TourRes,
  BookingCreateReq,
  BookingRes,
  TourScheduleRes
} from 'api-client'

const route = useRoute()
const { t, locale } = useI18n()
const localePath = useLocalePath()
const authStore = useAuthStore()
const toast = useToast()
const { formatPrice: formatCurrency } = useCurrency()

// Tour ID from route
const tourId = computed(() => route.params.id as string)
const { data: tour } = await useFetch<TourRes>(`/api/tours/${tourId.value}`)

// Fetch available schedules
const { data: schedules } = await useFetch<TourScheduleRes[]>(
  `/api/tours/${tourId.value}/schedules`
)

// Steps configuration
const steps = computed(() => [
  { title: t('booking.select_date') },
  { title: t('booking.select_participants') },
  { title: t('booking.participant_details') },
  { title: t('booking.payment') },
  { title: t('booking.confirmation') }
])

// Current step
const currentStep = ref(0)

// Payment processing state
const isProcessingPayment = ref(false)
const confirmedBooking = ref<BookingRes | null>(null)

// Booking state
const bookingState = reactive({
  scheduleId: null as string | null,
  totalParticipants: 1,
  participants: [] as Array<{
    fullName: string
    documentId: string
    nationality: string
    dateOfBirth: string | null
    pickupAddress: string
    specialRequirements: string
    phoneNumber: string
    email: string
  }>
})

// Initialize participants when adults/children change
watch(
  () => bookingState.totalParticipants,
  () => {
    bookingState.participants = []

    for (let i = 0; i < bookingState.totalParticipants; i++) {
      bookingState.participants.push(createEmptyParticipant())
    }
  }
)

function createEmptyParticipant() {
  return {
    fullName: '',
    documentId: '',
    nationality: '',
    dateOfBirth: null,
    pickupAddress: '',
    specialRequirements: '',
    phoneNumber: '',
    email: ''
  }
}

function selectSchedule(schedule: TourScheduleRes) {
  if (!schedule.id) return
  bookingState.scheduleId = schedule.id
}

type BookingParticipant = BookingCreateReq['participants'][number]

function updateParticipant(index: number, data: Partial<BookingParticipant>) {
  const current = bookingState.participants[index]
  if (current) {
    bookingState.participants[index] = {
      fullName: current.fullName || '',
      documentId: current.documentId || '',
      nationality: current.nationality || '',
      dateOfBirth: current.dateOfBirth || null,
      pickupAddress: current.pickupAddress || '',
      specialRequirements: current.specialRequirements || '',
      phoneNumber: current.phoneNumber || '',
      email: current.email || '',
      ...data
    }
  }
}

function copyUserDataToFirstParticipant() {
  if (!authStore.user || bookingState.participants.length === 0) return

  const user = authStore.user
  if (!bookingState.participants[0]) {
    bookingState.participants.push(createEmptyParticipant())
  }
  const firstParticipant = bookingState.participants[0]
  if (firstParticipant) {
    bookingState.participants[0] = {
      ...firstParticipant,
      fullName: user.fullName,
      nationality: user.nationality ?? '',
      dateOfBirth: user.dateOfBirth ?? null,
      documentId: firstParticipant.documentId ?? '',
      pickupAddress: firstParticipant.pickupAddress ?? '',
      specialRequirements: firstParticipant.specialRequirements ?? '',
      phoneNumber: firstParticipant.phoneNumber ?? '',
      email: firstParticipant.email ?? ''
    }
  }

  toast.add({
    title: t('common.success'),
    description: t('booking.copy_user_data_help'),
    color: 'success'
  })
}

function getTourName(tour: TourRes): string {
  const name
    = tour.nameTranslations?.[locale.value] || tour.nameTranslations?.es || ''
  return name
}

function formatScheduleDate(datetime: string | undefined): string {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleDateString(locale.value, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function calculateTotal(): string {
  if (!tour.value) return formatCurrency(0)
  const total = tour.value.price * bookingState.totalParticipants
  return formatCurrency(total)
}

// Validation
const canProceed = computed(() => {
  if (currentStep.value === 0) {
    return bookingState.scheduleId !== null
  }
  if (currentStep.value === 1) {
    return bookingState.totalParticipants > 0
  }
  if (currentStep.value === 2) {
    return bookingState.participants.every(
      p =>
        p.fullName.trim() !== ''
        && p.documentId.trim() !== ''
        && p.pickupAddress.trim() !== ''
    )
  }
  return true
})

function nextStep() {
  if (canProceed.value) {
    currentStep.value++
  }
}

function previousStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

async function handleMockPayment() {
  if (!tour.value || !bookingState.scheduleId) {
    toast.add({
      title: t('common.error'),
      description: 'Missing tour or schedule information',
      color: 'error'
    })
    return
  }

  isProcessingPayment.value = true

  try {
    // Step 1: Create the booking with PENDING status
    const bookingPayload: BookingCreateReq = {
      scheduleId: bookingState.scheduleId!,
      participants: bookingState.participants.map(p => ({
        fullName: p.fullName,
        documentId: p.documentId,
        nationality: p.nationality,
        dateOfBirth: p.dateOfBirth || undefined,
        pickupAddress: p.pickupAddress,
        specialRequirements: p.specialRequirements || undefined,
        phoneNumber: p.phoneNumber || undefined,
        email: p.email || undefined
      })),
      languageCode: locale.value,
      specialRequests: ''
    }

    const createdBooking = await $fetch<BookingRes>('/api/bookings', {
      method: 'post',
      body: bookingPayload
    })

    if (!createdBooking) {
      throw new Error('Failed to create booking')
    }

    toast.add({
      title: t('common.success'),
      description: t('booking.booking_created_success'),
      color: 'success'
    })

    // Step 2: Confirm the booking with mock payment
    const confirmed = await $fetch<BookingRes>(
      `/api/bookings/${createdBooking.id}/confirm-mock`,
      {
        method: 'post'
      }
    )

    if (!confirmed) {
      throw new Error('Failed to confirm booking')
    }

    confirmedBooking.value = confirmed as BookingRes

    toast.add({
      title: t('booking.payment_success'),
      description: t('booking.booking_confirmed_success'),
      color: 'success'
    })

    // Step 3: Move to confirmation step
    currentStep.value = 4
  } catch (error) {
    console.error('Payment error:', error)
    const message
      = typeof error === 'object'
        && error
        && 'message' in error
        && typeof error.message === 'string'
        ? error.message
        : 'An unexpected error occurred'
    toast.add({
      title: t('booking.payment_error'),
      description: message,
      color: 'error'
    })
  } finally {
    isProcessingPayment.value = false
  }
}

definePageMeta({
  layout: 'default'
})
</script>
