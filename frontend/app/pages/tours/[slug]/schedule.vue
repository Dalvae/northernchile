<script setup lang="ts">
import type { TourRes } from 'api-client'

const route = useRoute()
const router = useRouter()
const { locale, t } = useI18n()
const toast = useToast()
const cartStore = useCartStore()
const authStore = useAuthStore()

const tourSlug = route.params.slug as string

const tour = ref<TourRes | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// Selected schedule for booking
const selectedSchedule = ref<any | null>(null)
const participantCount = ref(1)
const showParticipantModal = ref(false)

// Reference to the calendar component
const calendarRef = ref<any>(null)

const translatedName = computed(
  () =>
    tour.value?.nameTranslations?.[locale.value]
    || tour.value?.nameTranslations?.['es']
    || ''
)

const translatedDescription = computed(
  () =>
    tour.value?.descriptionTranslations?.[locale.value]
    || tour.value?.descriptionTranslations?.['es']
    || ''
)

// Fetch tour data
async function fetchTour() {
  try {
    const response = await $fetch(`/api/tours/slug/${tourSlug}`)
    tour.value = response as TourRes
  } catch (e: any) {
    error.value = t('schedule.error_loading_tour')
    console.error('Failed to fetch tour', e)
  } finally {
    loading.value = false
  }
}

// Handle schedule click from calendar
function handleScheduleClick(schedule: any, _tour: any) {
  selectedSchedule.value = schedule
  participantCount.value = 1
  showParticipantModal.value = true
}

// Add to cart
async function addToCart() {
  if (!selectedSchedule.value) return

  try {
    await cartStore.addItem({
      scheduleId: selectedSchedule.value.id,
      numParticipants: participantCount.value
    })

    toast.add({
      color: 'success',
      title: t('schedule.added_to_cart'),
      description: `${translatedName.value} - ${new Date(
        selectedSchedule.value.startDatetime
      ).toLocaleDateString(locale.value)}`
    })

    showParticipantModal.value = false
    selectedSchedule.value = null

    // Go to cart
    router.push('/cart')
  } catch (e: any) {
    toast.add({
      color: 'error',
      title: t('common.error'),
      description: e.data?.message || t('schedule.error_adding_to_cart')
    })
  }
}

// Load tour on mount
onMounted(async () => {
  await fetchTour()
})

// SEO
useSeoMeta({
  title: () => `${translatedName.value} - ${t('schedule.title')} - Northern Chile`,
  description: translatedDescription,
  ogTitle: () => `${translatedName.value} - ${t('schedule.title')}`,
  ogDescription: translatedDescription,
  ogImage: tour.value?.images?.[0]?.imageUrl || 'https://www.northernchile.cl/og-image-tours.jpg',
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-900">
    <UContainer class="py-8 sm:py-12">
      <!-- Loading State -->
      <div
        v-if="loading"
        class="flex justify-center items-center py-20"
      >
        <UIcon
          name="i-lucide-loader-2"
          class="w-12 h-12 animate-spin text-primary"
        />
      </div>

      <!-- Error State -->
      <div
        v-else-if="error"
        class="text-center py-20"
      >
        <div
          class="w-16 h-16 mx-auto rounded-full bg-error/10 flex items-center justify-center mb-4"
        >
          <UIcon
            name="i-lucide-alert-circle"
            class="w-8 h-8 text-error"
          />
        </div>
        <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ error }}
        </h2>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          {{ t("schedule.error_description") }}
        </p>
        <UButton
          color="primary"
          @click="router.push('/tours')"
        >
          {{ t("schedule.back_to_tours") }}
        </UButton>
      </div>

      <!-- Tour Schedule Content -->
      <div v-else-if="tour">
        <!-- Tour Header -->
        <div class="mb-8">
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-arrow-left"
            class="mb-4"
            @click="router.push('/tours')"
          >
            {{ t("common.back") }}
          </UButton>

          <h1 class="text-4xl font-bold text-neutral-900 dark:text-white mb-4">
            {{ translatedName }}
          </h1>

          <p class="text-lg text-neutral-600 dark:text-neutral-400 mb-6">
            {{ translatedDescription }}
          </p>

          <!-- Tour Info Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <UCard>
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center"
                >
                  <UIcon
                    name="i-lucide-clock"
                    class="w-5 h-5 text-primary"
                  />
                </div>
                <div>
                  <p class="text-sm text-neutral-600 dark:text-neutral-400">
                    {{ t("tours.duration") }}
                  </p>
                  <p class="font-semibold text-neutral-900 dark:text-white">
                    {{ tour.durationHours }} {{ t("common.hours") }}
                  </p>
                </div>
              </div>
            </UCard>

            <UCard>
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center"
                >
                  <UIcon
                    name="i-lucide-tag"
                    class="w-5 h-5 text-primary"
                  />
                </div>
                <div>
                  <p class="text-sm text-neutral-600 dark:text-neutral-400">
                    {{ t("tours.price_from") }}
                  </p>
                  <p class="font-semibold text-neutral-900 dark:text-white">
                    ${{ (tour.price || 0)?.toLocaleString() }}
                  </p>
                </div>
              </div>
            </UCard>

            <UCard>
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center"
                >
                  <UIcon
                    name="i-lucide-users"
                    class="w-5 h-5 text-primary"
                  />
                </div>
                <div>
                  <p class="text-sm text-neutral-600 dark:text-neutral-400">
                    {{ t("tours.max_participants_label") }}
                  </p>
                  <p class="font-semibold text-neutral-900 dark:text-white">
                    {{ t("tours.max_participants", { count: tour.defaultMaxParticipants }) }}
                  </p>
                </div>
              </div>
            </UCard>

            <UCard>
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center"
                >
                  <UIcon
                    name="i-lucide-tag"
                    class="w-5 h-5 text-primary"
                  />
                </div>
                <div>
                  <p class="text-sm text-neutral-600 dark:text-neutral-400">
                    {{ t("tours.category.label") }}
                  </p>
                  <p class="font-semibold text-neutral-900 dark:text-white">
                    {{ t(`tours.category.${tour.category}`) }}
                  </p>
                </div>
              </div>
            </UCard>
          </div>
        </div>

        <!-- Calendar Section -->
        <div>
          <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-6">
            {{ t("schedule.select_date") || "Selecciona una fecha" }}
          </h2>

          <TourCalendar
            ref="calendarRef"
            :tours="[tour]"
            @schedule-click="handleScheduleClick"
          >
            <template #info>
              <div class="mt-4 p-4 bg-info/10 dark:bg-info/20 rounded-lg">
                <p class="text-sm text-info dark:text-info">
                  <UIcon
                    name="i-lucide-info"
                    class="inline w-4 h-4 mr-1"
                  />
                  {{ t("schedule.info_text") || "Haz clic en una fecha disponible para reservar tu lugar." }}
                </p>
              </div>
            </template>
          </TourCalendar>
        </div>

        <!-- Participant Count Modal -->
        <UModal v-model:open="showParticipantModal">
          <template #content>
            <div class="p-6">
              <!-- Header -->
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-neutral-900 dark:text-white">
                  {{ t("schedule.select_participants") || "Selecciona cantidad de participantes" }}
                </h3>
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-x"
                  size="sm"
                  @click="showParticipantModal = false"
                />
              </div>

              <!-- Selected Schedule Info -->
              <div
                v-if="selectedSchedule"
                class="mb-6 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
              >
                <p class="text-sm text-neutral-600 dark:text-neutral-400 mb-1">
                  {{ t("schedule.selected_date") }}
                </p>
                <p class="font-semibold text-neutral-900 dark:text-white">
                  {{ new Date(selectedSchedule.startDatetime).toLocaleDateString(locale, {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                  }) }}
                </p>
                <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-2">
                  {{ t("schedule.start_time") }}: {{ new Date(selectedSchedule.startDatetime).toLocaleTimeString(locale, {
                    hour: '2-digit',
                    minute: '2-digit'
                  }) }}
                </p>
              </div>

              <!-- Participant Count -->
              <div class="mb-6">
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2">
                  {{ t("schedule.number_of_participants") || "Número de participantes" }}
                </label>
                <div class="flex items-center gap-4">
                  <UButton
                    color="neutral"
                    variant="outline"
                    icon="i-lucide-minus"
                    size="lg"
                    :disabled="participantCount <= 1"
                    @click="participantCount--"
                  />
                  <input
                    v-model.number="participantCount"
                    type="number"
                    min="1"
                    :max="selectedSchedule?.maxParticipants || 20"
                    class="w-20 text-center text-2xl font-bold bg-transparent text-neutral-900 dark:text-white"
                  >
                  <UButton
                    color="neutral"
                    variant="outline"
                    icon="i-lucide-plus"
                    size="lg"
                    :disabled="participantCount >= (selectedSchedule?.availableSpots || selectedSchedule?.maxParticipants || 20)"
                    @click="participantCount++"
                  />
                </div>
                <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-2">
                  {{ t("schedule.available_spots") }}: {{ selectedSchedule?.availableSpots || selectedSchedule?.maxParticipants || 0 }}
                </p>
              </div>

              <!-- Price Calculation -->
              <div class="mb-6 p-4 bg-primary/5 rounded-lg">
                <div class="flex justify-between items-center">
                  <span class="text-neutral-700 dark:text-neutral-300">
                    {{ t("schedule.total_price") }}
                  </span>
                  <span class="text-2xl font-bold text-primary">
                    ${{ ((tour.price || 0) * participantCount).toLocaleString() }}
                  </span>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex gap-3">
                <UButton
                  color="neutral"
                  variant="outline"
                  class="flex-1"
                  @click="showParticipantModal = false"
                >
                  {{ t("common.cancel") }}
                </UButton>
                <UButton
                  color="primary"
                  class="flex-1"
                  @click="addToCart"
                >
                  {{ t("schedule.add_to_cart") }}
                </UButton>
              </div>
            </div>
          </template>
        </UModal>
      </div>
    </UContainer>
  </div>
</template>
