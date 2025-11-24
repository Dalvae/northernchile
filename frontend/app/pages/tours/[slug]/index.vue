<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TourRes } from 'api-client'
import { useCalendarData } from '~/composables/useCalendarData'
import { useCurrency } from '~/composables/useCurrency'

const route = useRoute()
const { locale } = useI18n()
const localePath = useLocalePath()
const { formatPrice } = useCurrency()
const tourSlug = route.params.slug as string

const {
  data: tour,
  pending: tourPending,
  error: tourError
} = await useFetch<TourRes>(`/api/tours/slug/${tourSlug}`)

const tourContent = ref<any>(null)

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
  } catch (e) {
    console.error(
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

const translatedContent = computed(() => {
  if (!tourContent.value) return null
  return tourContent.value[locale.value] || tourContent.value.es
})

const descriptionBlocks = computed(() => {
  const blocks
    = (tour.value as any)?.descriptionBlocksTranslations?.[locale.value]
      || (tour.value as any)?.descriptionBlocksTranslations?.es
  return Array.isArray(blocks)
    ? blocks.filter((b: any) => b?.type && b?.content)
    : []
})

const translatedDescription = computed(() => {
  if (descriptionBlocks.value.length) {
    return ''
  }
  return (
    (tour.value as any)?.descriptionTranslations?.[locale.value]
    || (tour.value as any)?.descriptionTranslations?.es
    || ''
  )
})

const translatedName = computed(
  () =>
    tour.value?.nameTranslations?.[locale.value]
    || tour.value?.nameTranslations?.es
    || ''
)

const heroImage = computed(() => {
  const hero = tour.value?.images?.find(
    (img: any) => img.isHeroImage
  )?.imageUrl
  const first = tour.value?.images?.[0]?.imageUrl
  return hero || first || 'default-image.jpg'
})

const featuredImages = computed(() => {
  return (
    tour.value?.images
      ?.filter((img: any) => img.isFeatured && !img.isHeroImage)
      .slice(0, 3) || []
  )
})

const galleryImages = computed(() => {
  return tour.value?.images?.filter((img: any) => !img.isHeroImage) || []
})

const seoDescription = computed(() => {
  if (descriptionBlocks.value.length > 0) {
    const firstTextBlock = descriptionBlocks.value.find(
      (b: any) => b.type === 'paragraph'
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
  title: () => `${translatedName.value} - Northern Chile Tours`,
  description: seoDescription,
  ogTitle: () => `${translatedName.value} - Northern Chile`,
  ogDescription: seoDescription,
  ogType: 'website',
  twitterCard: 'summary_large_image',
  twitterTitle: () => `${translatedName.value} - Northern Chile Tours`,
  twitterDescription: seoDescription,
  ogImageWidth: 1200,
  ogImageHeight: 630
})

// JSON-LD Structured Data for SEO (Schema.org Product)
useHead({
  script: [
    {
      type: 'application/ld+json',
      innerHTML: computed(() =>
        JSON.stringify({
          '@context': 'https://schema.org',
          '@type': 'Product',
          'name': translatedName.value,
          'description': seoDescription.value,
          'image': heroImage.value,
          'brand': {
            '@type': 'Brand',
            'name': 'Northern Chile Tours'
          },
          'offers': {
            '@type': 'Offer',
            'price': tour.value?.price,
            'priceCurrency': 'CLP',
            'availability': 'https://schema.org/InStock',
            'url': `https://www.northernchile.cl/tours/${tourSlug}`,
            'priceValidUntil': new Date(new Date().getFullYear() + 1, 11, 31)
              .toISOString()
              .split('T')[0],
            'seller': {
              '@type': 'Organization',
              'name': 'Northern Chile Tours'
            }
          },
          'aggregateRating': {
            '@type': 'AggregateRating',
            'ratingValue': '4.9',
            'reviewCount': '124',
            'bestRating': '5',
            'worstRating': '1'
          },
          'category': tour.value?.category || 'Astronomical Tour',
          'provider': {
            '@type': 'Organization',
            'name': 'Northern Chile Tours',
            'address': {
              '@type': 'PostalAddress',
              'addressLocality': 'San Pedro de Atacama',
              'addressRegion': 'Región de Antofagasta',
              'addressCountry': 'CL'
            }
          },
          'duration': `PT${tour.value?.durationHours || 0}H`,
          'tourBookingPage': `https://www.northernchile.cl/tours/${tourSlug}/schedule`
        })
      )
    }
  ]
})

async function goToSchedule() {
  const schedulePath = localePath(`/tours/${tourSlug}/schedule`)
  await navigateTo(schedulePath)
}

const showFloatingButton = ref(false)

onMounted(async () => {
  const handleScroll = () => {
    showFloatingButton.value = window.scrollY > 400
  }

  window.addEventListener('scroll', handleScroll)
  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  const today = new Date().toISOString().split('T')[0]

  try {
    const moonData = await fetchMoonPhases(today, today)
    if (moonData && moonData.length > 0) {
      const phase = moonData[0]
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
    console.error('Error cargando datos ambientales', e)
    currentMoonLabel.value = '-'
    currentWeatherLabel.value = '-'
  }
})

const lightboxOpen = ref(false)
const lightboxInitialIndex = ref(0)

function openLightbox(index: number) {
  lightboxInitialIndex.value = index
  lightboxOpen.value = true
}
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
              v-if="tour.itinerary && tour.itinerary.length"
              class="space-y-8"
            >
              <h2 class="text-3xl font-display font-bold text-white">
                Itinerario Estelar
              </h2>
              <div
                class="relative border-l-2 border-primary/30 ml-4 space-y-12 py-4"
              >
                <div
                  v-for="(item, index) in tour.itinerary"
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
                Lo Mejor del Tour
              </h2>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div
                  v-for="(img, index) in featuredImages"
                  :key="index"
                  class="relative aspect-[4/3] rounded-xl overflow-hidden atacama-card group"
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
                    class="absolute inset-0 bg-gradient-to-t from-neutral-950/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  />
                </div>
              </div>
            </div>

            <!-- Gallery Carousel -->
            <div
              v-if="galleryImages.length > 0"
              class="space-y-6"
            >
              <h2 class="text-3xl font-display font-bold text-white">
                Galería
              </h2>
              <UCarousel
                v-slot="{ item, index }"
                :items="galleryImages"
                :ui="{ item: 'basis-full md:basis-1/2 lg:basis-1/3 px-2' }"
                class="rounded-xl overflow-hidden"
                arrows
              >
                <div
                  class="relative aspect-[4/3] rounded-xl overflow-hidden atacama-card group cursor-pointer"
                  @click="openLightbox(index)"
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
                    <span class="text-neutral-300">Precio por persona</span>
                    <span class="text-white font-bold">${{ tour.price }}</span>
                  </div>
                  <div class="flex justify-between text-sm">
                    <span class="text-neutral-300">Duración</span>
                    <span class="text-white">{{ tour.durationHours }}h</span>
                  </div>
                </div>

                <UButton
                  block
                  size="xl"
                  color="primary"
                  class="cobre-glow font-bold"
                  @click="goToSchedule"
                >
                  Ver Disponibilidad
                </UButton>

                <p class="text-xs text-center text-neutral-500">
                  Cancelación gratuita hasta 24h antes
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

      <!-- Image Lightbox -->
      <CommonImageLightbox
        v-if="galleryImages.length > 0"
        v-model="lightboxOpen"
        :images="galleryImages"
        :initial-index="lightboxInitialIndex"
      />
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
              ${{ tour.price }}
              <span class="text-sm text-neutral-400 font-normal">/ persona</span>
            </p>
          </div>
          <UButton
            size="lg"
            color="primary"
            class="cobre-glow font-bold shadow-lg"
            icon="i-lucide-calendar-check"
            @click="goToSchedule"
          >
            Reservar
          </UButton>
        </div>
      </div>
    </Transition>
  </div>
</template>
