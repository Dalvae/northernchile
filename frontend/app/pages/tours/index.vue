<script setup lang="ts">
import type { TourRes, TourScheduleRes, ContentBlock, TourImageRes } from 'api-client'
import { useIntersectionObserver } from '@vueuse/core'

const router = useRouter()
const { t, locale } = useI18n()
const { formatPrice } = useCurrency()
const toast = useToast()
const cartStore = useCartStore()

const { data: allTours } = await useFetch<TourRes[]>('/api/tours', {
  transform: (tours) => {
    return tours.map(tour => ({
      id: tour.id,
      slug: tour.slug,
      nameTranslations: tour.nameTranslations,

      images: tour.images?.slice(0, 1),
      category: tour.category,
      price: tour.price,
      durationHours: tour.durationHours,

      defaultMaxParticipants: tour.defaultMaxParticipants,
      moonSensitive: tour.moonSensitive,
      windSensitive: tour.windSensitive,
      status: tour.status,

      descriptionBlocksTranslations: tour.descriptionBlocksTranslations
    }))
  }
})

const showCalendar = ref(false)
const calendarTriggerRef = ref(null)

useIntersectionObserver(
  calendarTriggerRef,
  (entries) => {
    const entry = entries[0]
    if (entry?.isIntersecting) {
      showCalendar.value = true
    }
  },
  { rootMargin: '300px' }
)

const sortedTours = computed(() => {
  return (allTours.value || [])
    .filter(t => t.status === 'PUBLISHED')
    .sort((a, b) => (a.price || 0) - (b.price || 0))
})

// ===== BOOKING MODAL =====
interface ScheduleWithTour extends TourScheduleRes {
  tour?: TourRes
}

const selectedSchedule = ref<ScheduleWithTour | null>(null)
const selectedTour = ref<TourRes | null>(null)
const participantCount = ref(1)
const showParticipantModal = ref(false)

function handleScheduleClick(schedule: TourScheduleRes, tour: TourRes) {
  selectedSchedule.value = { ...schedule, tour }
  selectedTour.value = tour
  participantCount.value = 1
  showParticipantModal.value = true
}

// Format schedule date for display
function formatScheduleDate(datetime: string) {
  const date = new Date(datetime)
  return date.toLocaleDateString(locale.value, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatScheduleTime(datetime: string) {
  const date = new Date(datetime)
  return date.toLocaleTimeString(locale.value, {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Computed for max participants in modal
const maxParticipantsForSelected = computed(() => {
  return selectedSchedule.value?.availableSpots ?? 1
})

// Total price computed
const totalPrice = computed(() => {
  return (selectedTour.value?.price || 0) * participantCount.value
})

// Add to cart
async function addToCart() {
  if (!selectedSchedule.value?.id) return

  try {
    await cartStore.addItem({
      scheduleId: selectedSchedule.value.id,
      numParticipants: participantCount.value
    })

    const tourName = selectedTour.value?.nameTranslations?.[locale.value]
      || selectedTour.value?.nameTranslations?.es
      || 'Tour'

    toast.add({
      color: 'success',
      title: t('schedule.added_to_cart'),
      description: `${tourName} - ${formatScheduleDate(selectedSchedule.value.startDatetime!)}`
    })

    showParticipantModal.value = false
    selectedSchedule.value = null
    selectedTour.value = null

    router.push('/cart')
  } catch (e: unknown) {
    const errorMessage = (e && typeof e === 'object' && 'data' in e
      && e.data && typeof e.data === 'object' && 'message' in e.data)
      ? (e.data as { message?: string }).message
      : undefined

    toast.add({
      color: 'error',
      title: t('common.error'),
      description: errorMessage || t('schedule.error_adding_to_cart')
    })
  }
}

const getTourName = (tour: TourRes) =>
  tour.nameTranslations?.[locale.value] || tour.nameTranslations?.es || 'Tour'

const getTourDescription = (tour: TourRes) => {
  const blocks
    = tour.descriptionBlocksTranslations?.[locale.value]
      || tour.descriptionBlocksTranslations?.es
  if (Array.isArray(blocks) && blocks.length) {
    const firstText = blocks.find((b: ContentBlock) => b?.content)?.content
    if (firstText) return firstText
  }
  return ''
}

const getTourImage = (tour: TourRes) => {
  const hero = tour.images?.find((img: TourImageRes) => img.isHeroImage)?.imageUrl
  const first = tour.images?.[0]?.imageUrl
  return hero || first || '/images/tour-placeholder.svg'
}

defineOgImageComponent('Tour', {
  title: t('tours.all'),
  category: 'Catálogo',
  image: '/images/catalogo-cover.jpg',
  price: 'Todos los Tours'
})

useSeoMeta({
  title: () => `${t('tours.all')} - Northern Chile`,
  description: () => t('tours.seo_description'),
  ogTitle: () => `${t('tours.all')} - Northern Chile`,
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <div class="min-h-screen relative overflow-hidden">
    <div
      class="absolute inset-0 bg-gradient-to-b from-neutral-950 to-neutral-900 z-0"
    />
    <div class="estrellas-atacama absolute inset-0 z-0" />

    <UContainer class="py-16 relative z-10">
      <div class="mb-20 text-center">
        <h1
          class="text-5xl md:text-7xl font-display font-bold text-white mb-6 text-glow"
        >
          {{ t("tours.all") }}
        </h1>
        <p class="text-xl text-neutral-200 max-w-3xl mx-auto font-light">
          {{ t("tours.subtitle") }}
        </p>
      </div>

      <div class="space-y-32 mb-32">
        <div
          v-for="(tour, index) in sortedTours"
          :key="tour.id"
          class="group relative flex flex-col lg:flex-row gap-12 items-center"
          :class="{ 'lg:flex-row-reverse': index % 2 !== 0 }"
        >
          <div class="w-full lg:w-1/2 relative">
            <div
              class="relative aspect-[4/3] rounded-2xl overflow-hidden atacama-card group-hover:shadow-2xl group-hover:shadow-primary-500/20 transition-all duration-700"
            >
              <NuxtImg
                :src="getTourImage(tour)"
                :alt="getTourName(tour)"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                :loading="index < 2 ? 'eager' : 'lazy'"
                :fetchpriority="index < 2 ? 'high' : 'auto'"
                format="webp"
                sizes="100vw sm:50vw md:600px"
                placeholder
              />
              <div
                class="absolute inset-0 bg-gradient-to-t from-neutral-950/80 via-transparent to-transparent opacity-60"
              />

              <div
                class="absolute bottom-6 right-6 backdrop-blur-md bg-neutral-900/80 border border-white/10 px-6 py-3 rounded-full"
              >
                <span
                  class="text-sm text-neutral-300 uppercase tracking-wider mr-2"
                >{{ t("tours.price_from") }}</span>
                <span class="text-xl font-bold text-white">{{
                  formatPrice(tour.price)
                }}</span>
              </div>
            </div>

            <div
              class="absolute -z-10 w-full h-full border border-primary/30 rounded-2xl top-4 transition-transform duration-500"
              :class="
                index % 2 === 0
                  ? 'left-4 group-hover:translate-x-2 group-hover:translate-y-2'
                  : 'right-4 group-hover:-translate-x-2 group-hover:translate-y-2'
              "
            />
          </div>

          <div class="w-full lg:w-1/2 space-y-6">
            <div class="flex items-center gap-3 mb-2">
              <UBadge
                color="primary"
                variant="soft"
                class="uppercase tracking-widest"
              >
                {{ t(`tours.category.${tour.category}`) || tour.category }}
              </UBadge>
              <div
                v-if="tour.moonSensitive"
                class="flex items-center gap-1 text-xs text-tertiary-400"
              >
                <UIcon
                  name="i-lucide-moon"
                  class="w-3 h-3"
                />
                <span>{{ t("tours.sensitive.moon") }}</span>
              </div>
            </div>

            <h2
              class="text-4xl md:text-5xl font-display font-bold text-white text-glow leading-tight"
            >
              {{ getTourName(tour) }}
            </h2>

            <p class="text-lg text-neutral-200 leading-relaxed line-clamp-4">
              {{ getTourDescription(tour) }}
            </p>

            <div
              class="flex flex-wrap gap-6 text-neutral-300 py-4 border-y border-white/10"
            >
              <div class="flex items-center gap-2">
                <UIcon
                  name="i-lucide-clock"
                  class="w-5 h-5 text-primary"
                />
                <span>{{
                  t("tours.duration_hours", { hours: tour.durationHours })
                }}</span>
              </div>
              <div class="flex items-center gap-2">
                <UIcon
                  name="i-lucide-users"
                  class="w-5 h-5 text-primary"
                />
                <span>{{
                  t("tours.max_participants", {
                    count: tour.defaultMaxParticipants || 10
                  })
                }}</span>
              </div>
            </div>

            <div class="pt-4">
              <UButton
                :to="`/tours/${tour.slug || tour.id}`"
                size="xl"
                color="primary"
                variant="solid"
                class="px-8 py-3 text-lg font-bold cobre-glow hover:scale-105 transition-transform"
                trailing-icon="i-lucide-arrow-right"
              >
                {{ t("tours.view_details") }}
              </UButton>
            </div>
          </div>
        </div>
      </div>

      <!-- SECCIÓN DEL CALENDARIO OPTIMIZADA -->
      <div
        ref="calendarTriggerRef"
        class="mt-32"
      >
        <!-- Referencia del Observador -->
        <div class="text-center mb-12">
          <h2
            class="text-3xl md:text-4xl font-display font-bold text-white mb-4 text-glow"
          >
            {{ t("tours.calendar_title") }}
          </h2>
          <p class="text-neutral-300 mb-8">
            {{ t("tours.calendar_subtitle") }}
          </p>
        </div>

        <!-- ESTADO DE CARGA (SKELETON) -->
        <!-- Se muestra mientras el usuario baja y el JS del calendario se descarga -->
        <div
          v-if="!showCalendar"
          class="atacama-card rounded-xl h-[600px] flex items-center justify-center animate-pulse"
        >
          <div class="text-center">
            <UIcon
              name="i-lucide-calendar"
              class="w-16 h-16 text-neutral-700 mb-4 mx-auto"
            />
            <p class="text-neutral-500">
              {{ t("tours.loading_availability") }}
            </p>
          </div>
        </div>

        <!-- COMPONENTE PESADO CON LAZY -->
        <!-- Nuxt separará este código en un archivo JS aparte -->
        <LazyTourCalendar
          v-else
          :tours="sortedTours"
          class="atacama-card rounded-xl overflow-hidden shadow-2xl transition-opacity duration-500"
          @schedule-click="handleScheduleClick"
        >
          <template #info>
            <div
              class="mt-4 p-4 bg-info/10 rounded-lg border border-info/20 flex items-start gap-3"
            >
              <UIcon
                name="i-lucide-info"
                class="w-5 h-5 text-info-400 mt-0.5"
              />
              <p class="text-sm text-info-200">
                {{ t("tours.calendar.info_text") }}
              </p>
            </div>
          </template>
        </LazyTourCalendar>
      </div>

      <!-- Booking Modal -->
      <UModal v-model:open="showParticipantModal">
        <template #content>
          <div class="p-6">
            <!-- Header -->
            <div class="flex justify-between items-center mb-6">
              <h3 class="text-xl font-bold text-neutral-900 dark:text-white">
                {{ t('schedule.select_participants') }}
              </h3>
              <UButton
                color="neutral"
                variant="ghost"
                icon="i-lucide-x"
                size="sm"
                @click="showParticipantModal = false"
              />
            </div>

            <!-- Tour Info -->
            <div
              v-if="selectedTour"
              class="mb-4 p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg"
            >
              <p class="font-semibold text-neutral-900 dark:text-white">
                {{ getTourName(selectedTour) }}
              </p>
              <p class="text-sm text-neutral-500">
                {{ formatPrice(selectedTour.price || 0) }} / {{ t('common.person') }}
              </p>
            </div>

            <!-- Selected Schedule Info -->
            <div
              v-if="selectedSchedule"
              class="mb-6 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
            >
              <p class="text-sm text-neutral-600 dark:text-neutral-300 mb-1">
                {{ t('schedule.selected_date') }}
              </p>
              <p class="font-semibold text-neutral-900 dark:text-white">
                {{ formatScheduleDate(selectedSchedule.startDatetime!) }}
                -
                {{ formatScheduleTime(selectedSchedule.startDatetime!) }}
              </p>
              <p class="text-sm text-neutral-500 mt-1">
                {{ selectedSchedule.availableSpots }} {{ t('schedule.spots_available') }}
              </p>
            </div>

            <!-- Participant Counter -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-3">
                {{ t('schedule.number_of_participants') }}
              </label>
              <div class="flex items-center justify-center gap-4">
                <UButton
                  color="neutral"
                  variant="outline"
                  icon="i-lucide-minus"
                  size="lg"
                  :disabled="participantCount <= 1"
                  @click="participantCount--"
                />
                <span class="text-3xl font-bold text-neutral-900 dark:text-white w-16 text-center">
                  {{ participantCount }}
                </span>
                <UButton
                  color="neutral"
                  variant="outline"
                  icon="i-lucide-plus"
                  size="lg"
                  :disabled="participantCount >= maxParticipantsForSelected"
                  @click="participantCount++"
                />
              </div>
            </div>

            <!-- Price Summary -->
            <div class="mb-6 p-4 bg-primary/10 rounded-lg">
              <div class="flex justify-between items-center">
                <span class="text-neutral-700 dark:text-neutral-200">
                  {{ formatPrice(selectedTour?.price || 0) }} x {{ participantCount }}
                </span>
                <span class="text-2xl font-bold text-primary">
                  {{ formatPrice(totalPrice) }}
                </span>
              </div>
            </div>

            <!-- Add to Cart Button -->
            <UButton
              block
              size="xl"
              color="primary"
              class="font-bold"
              icon="i-lucide-shopping-cart"
              @click="addToCart"
            >
              {{ t('schedule.add_to_cart') }}
            </UButton>
          </div>
        </template>
      </UModal>
    </UContainer>
  </div>
</template>
