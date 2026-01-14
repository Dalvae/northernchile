<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TourRes, TourScheduleRes, ContentBlock, TourImageRes, ItineraryItem } from 'api-client'
import { useCalendarData } from '~/composables/useCalendarData'
import { logger } from '~/utils/logger'
import { useCurrency } from '~/composables/useCurrency'
import { getTodayString, getLocalDateString } from '~/utils/dateUtils'
import { useIntersectionObserver } from '@vueuse/core'

const route = useRoute()
const router = useRouter()
const { locale, t } = useI18n()
const toast = useToast()
const cartStore = useCartStore()
const { formatPrice } = useCurrency()
const tourSlug = route.params.slug as string

const {
  data: tour,
  pending: tourPending,
  error: tourError
} = await useFetch<TourRes>(`/api/tours/slug/${tourSlug}`)

interface TourContent {
  [key: string]: unknown
}

const tourContent = ref<TourContent | null>(null)

const { fetchWeatherForecast, fetchMoonPhases } = useCalendarData()

const currentMoonLabel = ref('Cargando...')
const currentWeatherLabel = ref('Cargando...')

async function fetchContent(contentKey: string | undefined) {
  if (!contentKey) {
    tourContent.value = null
    return
  }
  try {
    const contentModule = await import(`~/app/content/tours/${contentKey}.ts`)
    tourContent.value = contentModule.default
  } catch {
    logger.error(
      'No se encontró contenido enriquecido para la clave:',
      contentKey
    )
    tourContent.value = null
  }
}

watch(
  tour,
  (newTour) => {
    if (newTour?.contentKey) {
      fetchContent(newTour.contentKey)
    }
  },
  { immediate: true }
)

const _translatedContent = computed(() => {
  if (!tourContent.value) return null
  return tourContent.value[locale.value] || tourContent.value.es
})

const descriptionBlocks = computed(() => {
  const blocks
    = tour.value?.descriptionBlocksTranslations?.[locale.value]
      || tour.value?.descriptionBlocksTranslations?.es
  return Array.isArray(blocks)
    ? blocks.filter((b: ContentBlock) => b?.type && b?.content)
    : []
})

const translatedItinerary = computed(() => {
  const itinerary
    = tour.value?.itineraryTranslations?.[locale.value]
      || tour.value?.itineraryTranslations?.es
      || tour.value?.itinerary
  return Array.isArray(itinerary) ? itinerary : []
})

const translatedDescription = computed(() => {
  if (descriptionBlocks.value.length) {
    return ''
  }
  return ''
})

const translatedName = computed(
  () =>
    tour.value?.nameTranslations?.[locale.value]
    || tour.value?.nameTranslations?.es
    || ''
)

const heroImage = computed(() => {
  const hero = tour.value?.images?.find(
    (img: TourImageRes) => img.isHeroImage
  )?.imageUrl
  const first = tour.value?.images?.[0]?.imageUrl
  return hero || first || 'default-image.jpg'
})

const featuredImages = computed(() => {
  return (
    tour.value?.images
      ?.filter((img: TourImageRes) => img.isFeatured && !img.isHeroImage)
      .slice(0, 6) || []
  )
})

const galleryImages = computed(() => {
  // Get IDs of featured images to exclude them from gallery
  const featuredIds = new Set(featuredImages.value.map(img => img.id))
  return (tour.value?.images?.filter((img: TourImageRes) =>
    !img.isHeroImage && !featuredIds.has(img.id)
  ) || [])
    .filter((img): img is TourImageRes & { imageUrl: string } => !!img.imageUrl)
})

// All images for lightbox (featured + gallery, in order)
const allLightboxImages = computed(() => {
  return [...featuredImages.value, ...galleryImages.value]
    .filter((img): img is TourImageRes & { imageUrl: string } => !!img.imageUrl)
})

const seoDescription = computed(() => {
  if (descriptionBlocks.value.length > 0) {
    const firstTextBlock = descriptionBlocks.value.find(
      (b: ContentBlock) => b.type === 'paragraph'
    )
    if (firstTextBlock?.content) {
      return firstTextBlock.content.substring(0, 200)
    }
  }
  if (translatedDescription.value) {
    return translatedDescription.value.substring(0, 200)
  }
  return `Descubre ${translatedName.value} en San Pedro de Atacama con Northern Chile Tours`
})

const categoryLabel = computed(() => {
  if (!tour.value?.category) return 'Northern Chile'
  return t(`tours.category.${tour.value.category}`)
})

defineOgImageComponent('Tour', computed(() => ({
  title: translatedName.value,
  price: formatPrice(tour.value?.price),
  image: heroImage.value,
  category: categoryLabel.value,
  width: 1200,
  height: 630
})))

useSeoMeta({
  title: () => `${translatedName.value} ${t('tours.location_suffix')}`,
  description: seoDescription,
  ogTitle: () => `${translatedName.value} ${t('tours.location_suffix')}`,
  ogDescription: seoDescription,
  ogType: 'website',
  twitterCard: 'summary_large_image',
  twitterTitle: () => `${translatedName.value} | Northern Chile Tours`,
  twitterDescription: seoDescription,
  ogImageWidth: 1200,
  ogImageHeight: 630
})

// JSON-LD Structured Data for SEO (Schema.org TouristTrip + Product)
useHead({
  script: [
    {
      type: 'application/ld+json',
      innerHTML: computed(() =>
        JSON.stringify({
          '@context': 'https://schema.org',
          '@type': ['TouristTrip', 'Product'],
          'name': translatedName.value,
          'description': seoDescription.value,
          'image': heroImage.value,
          'touristType': tour.value?.category === 'ASTRONOMICAL' ? 'Stargazing enthusiasts' : 'Adventure travelers',
          'brand': {
            '@type': 'Brand',
            'name': 'Northern Chile Tours'
          },
          'offers': {
            '@type': 'Offer',
            'price': tour.value?.price,
            'priceCurrency': 'CLP',
            'availability': 'https://schema.org/InStock',
            'url': `https://www.northernchile.com/tours/${tourSlug}`,
            'priceValidUntil': getLocalDateString(new Date(new Date().getFullYear() + 1, 11, 31)),
            'seller': {
              '@type': 'TravelAgency',
              'name': 'Northern Chile Tours',
              'url': 'https://www.northernchile.com'
            }
          },
          'aggregateRating': {
            '@type': 'AggregateRating',
            'ratingValue': '4.9',
            'reviewCount': '124',
            'bestRating': '5',
            'worstRating': '1'
          },
          'itinerary': {
            '@type': 'ItemList',
            'numberOfItems': translatedItinerary.value?.length || 0,
            'itemListElement': translatedItinerary.value?.map((step: ItineraryItem | string, idx: number) => ({
              '@type': 'ListItem',
              'position': idx + 1,
              'name': typeof step === 'string' ? step : step.description
            })) || []
          },
          'touristAttraction': {
            '@type': 'TouristAttraction',
            'name': 'San Pedro de Atacama',
            'description': 'Desert town in northern Chile, gateway to the Atacama Desert and world-renowned stargazing destination',
            'address': {
              '@type': 'PostalAddress',
              'addressLocality': 'San Pedro de Atacama',
              'addressRegion': 'Región de Antofagasta',
              'addressCountry': 'CL'
            }
          },
          'provider': {
            '@type': 'TravelAgency',
            'name': 'Northern Chile Tours',
            'url': 'https://www.northernchile.com',
            'address': {
              '@type': 'PostalAddress',
              'addressLocality': 'San Pedro de Atacama',
              'addressRegion': 'Región de Antofagasta',
              'addressCountry': 'CL'
            },
            'geo': {
              '@type': 'GeoCoordinates',
              'latitude': -22.9087,
              'longitude': -68.1997
            }
          },
          'duration': `PT${tour.value?.durationHours || 0}H`,
          'maximumAttendeeCapacity': tour.value?.defaultMaxParticipants || 12
        })
      )
    }
  ]
})

const showFloatingButton = ref(false)

onMounted(async () => {
  const handleScroll = () => {
    showFloatingButton.value = window.scrollY > 400
  }

  window.addEventListener('scroll', handleScroll)
  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  const today = getTodayString()

  try {
    const moonData = await fetchMoonPhases(today, today)
    if (moonData && moonData.length > 0) {
      const phase = moonData[0]!
      currentMoonLabel.value = `${phase.phaseName} (${phase.illumination}%)`
    } else {
      currentMoonLabel.value = 'No disponible'
    }

    const weatherMap = await fetchWeatherForecast()
    const todayWeather = weatherMap.get(today)

    if (todayWeather) {
      const temp = Math.round(todayWeather.temp.day)
      const clouds = todayWeather.clouds

      if (clouds < 10) currentWeatherLabel.value = `Despejado ${temp}°C`
      else if (clouds < 50) currentWeatherLabel.value = `Parcial ${temp}°C`
      else currentWeatherLabel.value = `Nublado ${temp}°C`
    } else {
      currentWeatherLabel.value = 'Sin pronóstico'
    }
  } catch (e) {
    logger.error('Error cargando datos ambientales', e)
    currentMoonLabel.value = '-'
    currentWeatherLabel.value = '-'
  }
})

const lightboxOpen = ref(false)
const lightboxInitialIndex = ref(0)

function openFeaturedLightbox(index: number) {
  // Featured images are at the beginning of allLightboxImages
  lightboxInitialIndex.value = index
  lightboxOpen.value = true
}

function openGalleryLightbox(index: number) {
  // Gallery images come after featured images in allLightboxImages
  lightboxInitialIndex.value = featuredImages.value.length + index
  lightboxOpen.value = true
}

// ===== BOOKING: Próximas salidas y calendario inline =====

// All schedules for this tour (shared between sidebar and calendar)
// TourScheduleRes already contains tourId, tourName, tourNameTranslations
const allSchedules = ref<TourScheduleRes[]>([])
const loadingSchedules = ref(true)

// Upcoming schedules for sidebar (filtered from allSchedules)
const upcomingSchedules = computed(() => {
  return allSchedules.value
    .filter(s => (s.status === 'SCHEDULED' || s.status === 'OPEN') && (s.availableSpots ?? s.maxParticipants) > 0)
    .sort((a, b) => new Date(a.startDatetime!).getTime() - new Date(b.startDatetime!).getTime())
    .slice(0, 5)
})

// Selected schedule for booking modal
const selectedSchedule = ref<TourScheduleRes | null>(null)
const participantCount = ref(1)
const showParticipantModal = ref(false)

// Lazy loading for inline calendar
const showInlineCalendar = ref(false)
const calendarSectionRef = ref(null)

useIntersectionObserver(
  calendarSectionRef,
  (entries) => {
    const entry = entries[0]
    if (entry?.isIntersecting) {
      showInlineCalendar.value = true
    }
  },
  { rootMargin: '200px' }
)

// Fetch all schedules for this tour (used by both sidebar and calendar)
async function fetchSchedules() {
  if (!tour.value?.id) return

  try {
    loadingSchedules.value = true

    // Set date range: today to 90 days from now
    const start = new Date()
    start.setHours(0, 0, 0, 0)
    const end = new Date()
    end.setDate(end.getDate() + 90)

    const response = await $fetch<TourScheduleRes[]>(`/api/tours/${tour.value.id}/schedules`, {
      params: {
        start: getLocalDateString(start),
        end: getLocalDateString(end)
      }
    })

    // Store all schedules (TourScheduleRes already contains tour info)
    allSchedules.value = response || []
  } catch (e) {
    logger.error('Error fetching schedules:', e)
    allSchedules.value = []
  } finally {
    loadingSchedules.value = false
  }
}

// Format schedule date for display
function formatScheduleDate(datetime: string) {
  const date = new Date(datetime)
  return date.toLocaleDateString(locale.value, {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  })
}

function formatScheduleTime(datetime: string) {
  const date = new Date(datetime)
  return date.toLocaleTimeString(locale.value, {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Open booking modal for a schedule
function openBookingModal(schedule: TourScheduleRes) {
  selectedSchedule.value = schedule
  participantCount.value = 1
  showParticipantModal.value = true
}

// Handle schedule click from inline calendar
// Note: _tour param comes from calendar but we use tour.value from this page
function handleCalendarScheduleClick(schedule: TourScheduleRes, _tour: TourRes | undefined) {
  openBookingModal(schedule)
}

// Computed for max participants in modal
const maxParticipantsForSelected = computed(() => {
  return selectedSchedule.value?.availableSpots ?? 1
})

// Total price computed
const totalPrice = computed(() => {
  return (tour.value?.price || 0) * participantCount.value
})

// Track if adding to cart is in progress for this schedule
const isAddingToCart = computed(() => {
  return selectedSchedule.value?.id
    ? cartStore.isOperationPending(`add:${selectedSchedule.value.id}`)
    : false
})

// Add to cart with duplicate-click protection
async function addToCart() {
  if (!selectedSchedule.value?.id) return

  // cartStore.addItem now handles duplicate protection internally
  const success = await cartStore.addItem({
    scheduleId: selectedSchedule.value.id,
    numParticipants: participantCount.value
  })

  if (success) {
    toast.add({
      color: 'success',
      title: t('schedule.added_to_cart'),
      description: `${translatedName.value} - ${formatScheduleDate(selectedSchedule.value.startDatetime!)}`
    })

    showParticipantModal.value = false
    selectedSchedule.value = null
    router.push('/cart')
  }
  // Error toast is handled by cartStore.addItem
}

// Scroll to calendar section
function scrollToCalendar() {
  const element = document.getElementById('calendario-disponibilidad')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// Fetch schedules when tour is loaded
watch(tour, (newTour) => {
  if (newTour?.id) {
    fetchSchedules()
  }
}, { immediate: true })
</script>

<template>
  <div class="min-h-screen bg-neutral-950 relative">
    <div
      v-if="tourPending"
      class="h-screen flex items-center justify-center text-neutral-300"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin"
      />
    </div>

    <div
      v-else-if="tourError"
      class="h-screen flex items-center justify-center"
    >
      <UAlert
        color="error"
        :title="tourError.message"
        class="max-w-md"
      />
    </div>

    <div v-else-if="tour">
      <div class="relative h-screen min-h-[600px] w-full overflow-hidden">
        <NuxtImg
          :src="heroImage"
          :alt="translatedName"
          class="absolute inset-0 w-full h-full object-cover"
          format="webp"
          loading="eager"
          fetchpriority="high"
        />
        <div
          class="absolute inset-0 bg-gradient-to-t from-neutral-950 via-neutral-950/40 to-transparent"
        />

        <div
          class="absolute bottom-0 left-0 right-0 p-8 pb-24 sm:p-16 sm:pb-32 max-w-7xl mx-auto"
        >
          <UBadge
            v-if="tour.category"
            :label="tour.category"
            color="primary"
            variant="solid"
            class="mb-4 backdrop-blur-md"
          />
          <h1
            class="text-5xl md:text-7xl font-display font-bold text-white mb-6 text-glow leading-tight"
          >
            {{ translatedName }}
          </h1>
          <div class="flex flex-wrap gap-6 text-lg text-neutral-200">
            <div class="flex items-center gap-2">
              <UIcon
                name="i-lucide-clock"
                class="w-5 h-5 text-primary"
              />
              <span>{{ tour.durationHours }} Horas</span>
            </div>
            <div class="flex items-center gap-2">
              <UIcon
                name="i-lucide-users"
                class="w-5 h-5 text-primary"
              />
              <span>Max {{ tour.defaultMaxParticipants }} personas</span>
            </div>
            <div class="flex items-center gap-2">
              <UIcon
                name="i-lucide-dollar-sign"
                class="w-5 h-5 text-primary"
              />
              <span>Desde ${{ tour.price }}</span>
            </div>
          </div>
        </div>
      </div>

      <UContainer class="relative z-10 -mt-20 pb-24">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
          <!-- Main Content (Left 2/3) -->
          <div class="lg:col-span-2 space-y-12">
            <!-- Description -->
            <div class="prose prose-lg prose-invert max-w-none">
              <div
                v-if="descriptionBlocks.length"
                class="space-y-6"
              >
                <component
                  :is="block.type === 'heading' ? 'h2' : 'p'"
                  v-for="(block, index) in descriptionBlocks"
                  :key="index"
                  :class="
                    block.type === 'heading'
                      ? 'text-3xl font-display font-bold text-white mt-8 mb-4'
                      : 'text-neutral-200 leading-relaxed'
                  "
                >
                  {{ block.content }}
                </component>
              </div>
              <p
                v-else-if="translatedDescription"
                class="text-neutral-200 leading-relaxed"
              >
                {{ translatedDescription }}
              </p>
            </div>

            <!-- Itinerary (Vertical Timeline) -->
            <div
              v-if="translatedItinerary && translatedItinerary.length"
              class="space-y-8"
            >
              <h2 class="text-3xl font-display font-bold text-white">
                {{ t('tours.itinerary_title') }}
              </h2>
              <div
                class="relative border-l-2 border-primary/30 ml-4 space-y-12 py-4"
              >
                <div
                  v-for="(item, index) in translatedItinerary"
                  :key="index"
                  class="relative pl-8"
                >
                  <div
                    class="absolute -left-[9px] top-0 w-4 h-4 rounded-full bg-primary border-4 border-neutral-950 shadow-[0_0_10px_var(--ui-color-primary-500)]"
                  />
                  <span class="text-primary font-bold text-lg block mb-1">{{
                    item.time
                  }}</span>
                  <p class="text-neutral-200">
                    {{ item.description }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Featured Images (Highlights) -->
            <div
              v-if="featuredImages.length > 0"
              class="space-y-6"
            >
              <h2 class="text-3xl font-display font-bold text-white">
                {{ t('tours.highlights_title') }}
              </h2>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div
                  v-for="(img, index) in featuredImages"
                  :key="img.id || index"
                  class="relative aspect-[4/3] rounded-xl overflow-hidden atacama-card group cursor-pointer"
                  @click="openFeaturedLightbox(index)"
                >
                  <NuxtImg
                    :src="img.imageUrl"
                    :alt="`Featured image ${index + 1}`"
                    class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                    format="webp"
                    loading="lazy"
                    placeholder
                  />
                  <div
                    class="absolute inset-0 bg-gradient-to-t from-neutral-950/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center"
                  >
                    <UIcon
                      name="i-heroicons-magnifying-glass-plus"
                      class="w-10 h-10 text-white"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Gallery Carousel -->
            <div
              v-if="galleryImages.length > 0"
              class="space-y-6"
            >
              <div class="flex items-center justify-between">
                <h2 class="text-3xl font-display font-bold text-white">
                  Galería
                </h2>
                <button
                  class="flex items-center gap-2 text-primary hover:text-primary/80 transition-colors group"
                  @click="openGalleryLightbox(0)"
                >
                  <span class="text-sm font-medium">{{ galleryImages.length }} {{ t('tours.images') }}</span>
                  <UIcon
                    name="i-lucide-images"
                    class="w-5 h-5 group-hover:scale-110 transition-transform"
                  />
                </button>
              </div>
              <UCarousel
                v-slot="{ item, index }"
                :items="galleryImages"
                :ui="{ item: 'basis-full md:basis-1/2 lg:basis-1/3 px-2' }"
                class="rounded-xl overflow-hidden"
                arrows
              >
                <div
                  class="relative aspect-[4/3] rounded-xl overflow-hidden atacama-card group cursor-pointer"
                  @click="openGalleryLightbox(index)"
                >
                  <NuxtImg
                    :src="item.imageUrl"
                    class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
                    draggable="false"
                    format="webp"
                    loading="lazy"
                    placeholder
                  />
                  <div
                    class="absolute inset-0 bg-gradient-to-t from-neutral-950/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center"
                  >
                    <UIcon
                      name="i-heroicons-magnifying-glass-plus"
                      class="w-12 h-12 text-white"
                    />
                  </div>
                </div>
              </UCarousel>
              <!-- View all images hint -->
              <div
                v-if="galleryImages.length > 3"
                class="text-center"
              >
                <button
                  class="inline-flex items-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 rounded-full text-sm text-neutral-300 hover:text-white transition-colors"
                  @click="openGalleryLightbox(0)"
                >
                  <UIcon
                    name="i-lucide-expand"
                    class="w-4 h-4"
                  />
                  {{ t('tours.view_all_images', { count: allLightboxImages.length }) }}
                </button>
              </div>
            </div>
          </div>

          <!-- Sidebar (Right 1/3) -->
          <div class="space-y-8">
            <!-- Sticky Booking Card -->
            <div class="sticky top-24 space-y-6">
              <div
                class="atacama-card p-6 rounded-xl space-y-6 backdrop-blur-xl bg-neutral-900/80"
              >
                <h3 class="text-xl font-bold text-white">
                  Reserva tu Experiencia
                </h3>

                <!-- Weather / Moon Info -->
                <div class="grid grid-cols-2 gap-4">
                  <div class="bg-white/5 p-3 rounded-lg text-center">
                    <UIcon
                      name="i-lucide-moon"
                      class="w-6 h-6 text-tertiary mb-1 mx-auto"
                    />
                    <span class="text-xs text-neutral-300 block">Fase Lunar (Hoy)</span>
                    <span class="text-sm font-medium text-white truncate block">
                      {{ currentMoonLabel }}
                    </span>
                  </div>
                  <div class="bg-white/5 p-3 rounded-lg text-center">
                    <UIcon
                      name="i-lucide-thermometer"
                      class="w-6 h-6 text-info mb-1 mx-auto"
                    />
                    <span class="text-xs text-neutral-300 block">Clima (Hoy)</span>
                    <span class="text-sm font-medium text-white truncate block">
                      {{ currentWeatherLabel }}
                    </span>
                  </div>
                </div>

                <div class="space-y-2">
                  <div class="flex justify-between text-sm">
                    <span class="text-neutral-300">{{ t('tours.price_per_person') }}</span>
                    <span class="text-white font-bold">{{ formatPrice(tour.price || 0) }}</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-neutral-300">{{ t('tours.duration') }}</span>
                    <span class="text-white">{{ tour.durationHours }}h</span>
                  </div>
                </div>

                <!-- Próximas Salidas -->
                <div class="space-y-3">
                  <h4 class="text-sm font-semibold text-neutral-300">
                    {{ t('schedule.upcoming_departures') }}
                  </h4>

                  <!-- Loading state -->
                  <div
                    v-if="loadingSchedules"
                    class="space-y-2"
                  >
                    <div
                      v-for="i in 3"
                      :key="i"
                      class="h-12 bg-white/5 rounded-lg animate-pulse"
                    />
                  </div>

                  <!-- No schedules -->
                  <div
                    v-else-if="upcomingSchedules.length === 0"
                    class="text-sm text-neutral-400 text-center py-4"
                  >
                    {{ t('schedule.no_upcoming') }}
                  </div>

                  <!-- Schedule buttons -->
                  <div
                    v-else
                    class="space-y-2"
                  >
                    <button
                      v-for="schedule in upcomingSchedules"
                      :key="schedule.id"
                      class="w-full flex items-center justify-between p-3 bg-white/5 hover:bg-white/10 rounded-lg transition-colors group"
                      @click="openBookingModal(schedule)"
                    >
                      <div class="flex items-center gap-3">
                        <UIcon
                          name="i-lucide-calendar"
                          class="w-4 h-4 text-primary"
                        />
                        <div class="text-left">
                          <span class="text-sm font-medium text-white block">
                            {{ formatScheduleDate(schedule.startDatetime!) }}
                          </span>
                          <span class="text-xs text-neutral-400">
                            {{ formatScheduleTime(schedule.startDatetime!) }}
                          </span>
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-xs text-neutral-400">
                          {{ schedule.availableSpots }}/{{ schedule.maxParticipants }}
                        </span>
                        <UIcon
                          name="i-lucide-chevron-right"
                          class="w-4 h-4 text-neutral-500 group-hover:text-primary transition-colors"
                        />
                      </div>
                    </button>
                  </div>
                </div>

                <!-- CTA Button -->
                <UButton
                  v-if="upcomingSchedules.length > 0"
                  block
                  size="xl"
                  color="primary"
                  class="cobre-glow font-bold"
                  icon="i-lucide-ticket"
                  @click="openBookingModal(upcomingSchedules[0]!)"
                >
                  {{ t('schedule.book_now') }}
                </UButton>

                <!-- Ver calendario completo -->
                <button
                  class="w-full text-center text-sm text-primary hover:text-primary/80 transition-colors flex items-center justify-center gap-1"
                  @click="scrollToCalendar"
                >
                  {{ t('schedule.view_full_calendar') }}
                  <UIcon
                    name="i-lucide-chevron-down"
                    class="w-4 h-4"
                  />
                </button>

                <p class="text-xs text-center text-neutral-500">
                  {{ t('schedule.free_cancellation') }}
                </p>
              </div>

              <!-- Sensitivities -->
              <div
                v-if="
                  tour.isWindSensitive
                    || tour.isMoonSensitive
                    || tour.isCloudSensitive
                "
                class="atacama-card p-6 rounded-xl space-y-4"
              >
                <h3 class="text-lg font-bold text-white">
                  Condiciones
                </h3>
                <ul class="space-y-3">
                  <li
                    v-if="tour.isMoonSensitive"
                    class="flex items-center gap-3 text-sm text-neutral-200"
                  >
                    <UIcon
                      name="i-lucide-moon"
                      class="w-5 h-5 text-tertiary shrink-0"
                    />
                    <span>Sensible a la fase lunar (mejor en Luna Nueva)</span>
                  </li>
                  <li
                    v-if="tour.isWindSensitive"
                    class="flex items-center gap-3 text-sm text-neutral-200"
                  >
                    <UIcon
                      name="i-lucide-wind"
                      class="w-5 h-5 text-info shrink-0"
                    />
                    <span>Sujeto a condiciones de viento</span>
                  </li>
                  <li
                    v-if="tour.isCloudSensitive"
                    class="flex items-center gap-3 text-sm text-neutral-200"
                  >
                    <UIcon
                      name="i-lucide-cloud"
                      class="w-5 h-5 text-neutral-300 shrink-0"
                    />
                    <span>Requiere cielos despejados</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </UContainer>

      <!-- Calendario de Disponibilidad (Inline) -->
      <UContainer class="pb-24">
        <div
          id="calendario-disponibilidad"
          ref="calendarSectionRef"
          class="scroll-mt-24"
        >
          <h2 class="text-3xl font-display font-bold text-white mb-6">
            {{ t('schedule.availability_calendar') }}
          </h2>

          <!-- Skeleton while loading -->
          <div
            v-if="!showInlineCalendar"
            class="animate-pulse bg-neutral-800 h-96 rounded-xl flex items-center justify-center"
          >
            <p class="text-neutral-400">
              {{ t('common.loading') }}...
            </p>
          </div>

          <!-- Lazy-loaded calendar -->
          <LazyTourCalendar
            v-if="showInlineCalendar"
            :tours="[tour]"
            :preloaded-schedules="allSchedules"
            @schedule-click="handleCalendarScheduleClick"
          >
            <template #info>
              <div class="mt-4 p-4 bg-info/10 rounded-lg">
                <p class="text-sm text-info">
                  <UIcon
                    name="i-lucide-info"
                    class="inline w-4 h-4 mr-1"
                  />
                  {{ t('schedule.calendar_info') }}
                </p>
              </div>
            </template>
          </LazyTourCalendar>
        </div>
      </UContainer>

      <!-- Image Lightbox -->
      <CommonImageLightbox
        v-if="allLightboxImages.length > 0"
        v-model="lightboxOpen"
        :images="allLightboxImages"
        :initial-index="lightboxInitialIndex"
      />

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

            <!-- Selected Schedule Info -->
            <div
              v-if="selectedSchedule"
              class="mb-6 p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg"
            >
              <p class="text-sm text-neutral-600 dark:text-neutral-300 mb-1">
                {{ t('schedule.selected_date') }}
              </p>
              <p class="font-semibold text-neutral-900 dark:text-white">
                {{ new Date(selectedSchedule.startDatetime!).toLocaleDateString(locale, {
                  weekday: 'long',
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                }) }}
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
                  {{ formatPrice(tour.price || 0) }} x {{ participantCount }}
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
              :loading="isAddingToCart"
              :disabled="isAddingToCart"
              @click="addToCart"
            >
              {{ t('schedule.add_to_cart') }}
            </UButton>
          </div>
        </template>
      </UModal>
    </div>

    <!-- Floating Mobile Booking Button -->
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="translate-y-full opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-full opacity-0"
    >
      <div
        v-if="tour && showFloatingButton"
        class="fixed bottom-0 left-0 right-0 z-50 lg:hidden bg-neutral-900/95 backdrop-blur-xl border-t border-neutral-800 shadow-2xl"
      >
        <div class="p-4 flex items-center justify-between gap-4">
          <div class="flex-1 min-w-0">
            <p class="text-sm text-neutral-400 truncate">
              {{ translatedName }}
            </p>
            <p class="text-xl font-bold text-white">
              {{ formatPrice(tour.price || 0) }}
              <span class="text-sm text-neutral-400 font-normal">/ {{ t('common.person') }}</span>
            </p>
          </div>
          <UButton
            v-if="upcomingSchedules.length > 0"
            size="lg"
            color="primary"
            class="cobre-glow font-bold shadow-lg"
            icon="i-lucide-ticket"
            @click="openBookingModal(upcomingSchedules[0]!)"
          >
            {{ t('schedule.book_now') }}
          </UButton>
          <UButton
            v-else
            size="lg"
            color="primary"
            class="cobre-glow font-bold shadow-lg"
            icon="i-lucide-calendar"
            @click="scrollToCalendar"
          >
            {{ t('schedule.view_calendar') }}
          </UButton>
        </div>
      </div>
    </Transition>
  </div>
</template>
