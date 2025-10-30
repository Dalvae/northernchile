<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-900 py-12">
    <UContainer>
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t('booking.title') }}
        </h1>
        <p v-if="tour" class="text-neutral-600 dark:text-neutral-400">
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
          <div v-if="currentStep === 0" class="space-y-6">
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t('booking.select_date') }}
            </h2>

            <div v-if="schedules && schedules.length > 0" class="grid gap-4">
              <UButton
                v-for="schedule in schedules"
                :key="schedule.id"
                :variant="bookingState.scheduleId === schedule.id ? 'solid' : 'outline'"
                :color="bookingState.scheduleId === schedule.id ? 'primary' : 'neutral'"
                size="lg"
                block
                @click="selectSchedule(schedule)"
                class="justify-between"
              >
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-calendar" class="w-5 h-5" />
                  <span>{{ formatScheduleDate(schedule.startDatetime) }}</span>
                </div>
                <UBadge v-if="schedule.availableSlots" :label="`${schedule.availableSlots} disponibles`" color="success" />
              </UButton>
            </div>
            <div v-else class="text-center py-8 text-neutral-600 dark:text-neutral-400">
              {{ t('common.loading') }}
            </div>
          </div>

          <!-- Step 2: Select Participants -->
          <div v-else-if="currentStep === 1" class="space-y-6">
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t('booking.select_participants') }}
            </h2>

            <div class="grid md:grid-cols-2 gap-6">
            <UFormField :label="t('booking.total_participants')" name="totalParticipants">
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
                <div class="flex justify-between text-neutral-700 dark:text-neutral-300">
                  <span>{{ bookingState.totalParticipants }} x {{ t('booking.participant') }}</span>
                  <span>${{ formatPrice(tour.price * bookingState.totalParticipants) }}</span>
                </div>
                <UDivider />
                <div class="flex justify-between text-lg font-semibold text-neutral-900 dark:text-white">
                  <span>{{ t('common.total') }}</span>
                  <span>${{ calculateTotal() }}</span>
                </div>
              </div>
            </UCard>
          </div>

          <!-- Step 3: Participant Details -->
          <div v-else-if="currentStep === 2" class="space-y-6">
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
                {{ t('booking.participant_details') }}
              </h2>
              <UButton
                v-if="authStore.user"
                variant="outline"
                color="primary"
                size="sm"
                icon="i-lucide-copy"
                @click="copyUserDataToFirstParticipant"
              >
                {{ t('booking.copy_user_data') }}
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
          <div v-else-if="currentStep === 3" class="space-y-6">
            <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ t('booking.payment') }}
            </h2>
            <p class="text-neutral-600 dark:text-neutral-400">
              Payment integration coming soon...
            </p>
          </div>

          <!-- Step 5: Confirmation -->
          <div v-else-if="currentStep === 4" class="space-y-6 text-center">
            <div class="w-16 h-16 mx-auto bg-success/10 rounded-full flex items-center justify-center">
              <UIcon name="i-lucide-check-circle" class="w-10 h-10 text-success" />
            </div>
            <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white">
              {{ t('booking.confirmation') }}
            </h2>
            <p class="text-neutral-600 dark:text-neutral-400">
              Your booking has been confirmed!
            </p>
          </div>
        </template>
      </UCard>

      <!-- Navigation Buttons -->
      <div class="flex justify-between max-w-4xl mx-auto mt-8">
        <UButton
          v-if="currentStep > 0"
          variant="outline"
          color="neutral"
          size="lg"
          icon="i-lucide-arrow-left"
          @click="previousStep"
        >
          {{ t('common.back') }}
        </UButton>
        <div v-else></div>

        <UButton
          v-if="currentStep < steps.length - 1"
          color="primary"
          size="lg"
          :disabled="!canProceed"
          @click="nextStep"
        >
          {{ t('common.next') }}
          <UIcon name="i-lucide-arrow-right" class="ml-2" />
        </UButton>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const { t, locale } = useI18n()
const authStore = useAuthStore()
const toast = useToast()

// Tour ID from route
const tourId = computed(() => route.params.id as string)

// Fetch tour data
const { data: tour } = await useFetch(`/api/tours/${tourId.value}`)

// Fetch available schedules
const { data: schedules } = await useFetch(`/api/tours/${tourId.value}/schedules`)

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

// Booking state
const bookingState = reactive({
  scheduleId: null as string | null,
  totalParticipants: 1,
  participants: [] as Array<{
    fullName: string
    documentId: string
    nationality: string
    age: number | null
    pickupAddress: string
    specialRequirements: string
  }>
})

// Initialize participants when adults/children change
watch(() => bookingState.totalParticipants, () => {
  bookingState.participants = []

  for (let i = 0; i < bookingState.totalParticipants; i++) {
    bookingState.participants.push(createEmptyParticipant())
  }
})

function createEmptyParticipant() {
  return {
    fullName: '',
    documentId: '',
    nationality: '',
    age: null,
    pickupAddress: '',
    specialRequirements: ''
  }
}

function selectSchedule(schedule: any) {
  bookingState.scheduleId = schedule.id
}

function updateParticipant(index: number, data: any) {
  bookingState.participants[index] = { ...bookingState.participants[index], ...data }
}

function copyUserDataToFirstParticipant() {
  if (!authStore.user || bookingState.participants.length === 0) return

  const user = authStore.user
  bookingState.participants[0] = {
    ...bookingState.participants[0],
    fullName: user.fullName || '',
    nationality: user.nationality || '',
    age: user.dateOfBirth ? calculateAge(user.dateOfBirth) : null
  }

  toast.add({
    title: t('common.success'),
    description: t('booking.copy_user_data_help'),
    color: 'success'
  })
}

function calculateAge(dateOfBirth: string): number {
  const today = new Date()
  const birthDate = new Date(dateOfBirth)
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

function getTourName(tour: any): string {
  const name = tour.nameTranslations?.[locale.value] || tour.nameTranslations?.es || ''
  return name
}

function formatScheduleDate(datetime: string): string {
  const date = new Date(datetime)
  return date.toLocaleDateString(locale.value, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatPrice(price: number): string {
  return price.toLocaleString('es-CL', { minimumFractionDigits: 0 })
}

function calculateTotal(): string {
  if (!tour.value) return '0'
  const total = tour.value.price * bookingState.totalParticipants
  return formatPrice(total)
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
    return bookingState.participants.every(p =>
      p.fullName.trim() !== '' &&
      p.documentId.trim() !== '' &&
      p.pickupAddress.trim() !== ''
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

definePageMeta({
  layout: 'default'
})
</script>
