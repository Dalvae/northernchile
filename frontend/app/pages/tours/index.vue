<script setup lang="ts">
import type { TourRes, TourScheduleRes } from 'api-client'

const router = useRouter()
const { t, locale } = useI18n()
const { formatPrice } = useCurrency()

const { fetchAll } = useTours()
const { data: allTours } = await fetchAll()

// Tours sorted by name or priority (if we had one)
const sortedTours = computed(() => {
  return (allTours.value || [])
    .filter(t => t.status === 'PUBLISHED')
    .sort((a, b) => (a.price || 0) - (b.price || 0)) // Sort by price for now, or maybe by popularity
})

// Handle calendar schedule click
function handleScheduleClick(schedule: TourScheduleRes, tour: TourRes) {
  if (tour?.slug) {
    router.push(`/tours/${tour.slug}/schedule`)
  }
}

// Helper to get localized name
const getTourName = (tour: TourRes) => 
  tour.nameTranslations?.[locale.value] || tour.nameTranslations?.es || 'Tour'

// Helper to get localized description
const getTourDescription = (tour: TourRes) => {
  const blocks = (tour as any).descriptionBlocksTranslations?.[locale.value]
    || (tour as any).descriptionBlocksTranslations?.es
  if (Array.isArray(blocks) && blocks.length) {
    const firstText = blocks.find((b: any) => b?.content)?.content
    if (firstText) return firstText
  }
  return (tour as any).descriptionTranslations?.[locale.value]
    || (tour as any).descriptionTranslations?.es
    || ''
}

// Helper to get image
const getTourImage = (tour: TourRes) => {
  const hero = tour.images?.find(img => (img as any).isHeroImage)?.imageUrl
  const first = tour.images?.[0]?.imageUrl || (tour as any).images?.[0]
  return hero || first || '/images/tour-placeholder.svg'
}

// SEO
useSeoMeta({
  title: () => `${t('tours.all')} - Northern Chile`,
  description:
    'Explora nuestra selección completa de tours astronómicos y experiencias únicas en San Pedro de Atacama. Encuentra el tour perfecto para ti.',
  ogTitle: () => `${t('tours.all')} - Northern Chile`,
  ogDescription:
    'Explora nuestra selección completa de tours astronómicos y experiencias únicas en San Pedro de Atacama',
  ogImage: 'https://www.northernchile.cl/og-image-tours.jpg',
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <div class="min-h-screen relative overflow-hidden">
    <!-- Background Gradient -->
    <div class="absolute inset-0 bg-gradient-to-b from-neutral-950 to-neutral-900 z-0" />
    <div class="estrellas-atacama absolute inset-0 z-0" />

    <UContainer class="py-16 relative z-10">
      <!-- Header -->
      <div class="mb-20 text-center">
        <h1 class="text-5xl md:text-7xl font-display font-bold text-white mb-6 text-glow">
          {{ t("tours.all") }}
        </h1>
        <p class="text-xl text-neutral-200 max-w-3xl mx-auto font-light">
          Descubre nuestras experiencias exclusivas bajo los cielos más limpios del mundo.
          Cada tour es una invitación a conectar con el cosmos.
        </p>
      </div>

      <!-- Tours List (Alternating Layout) -->
      <div class="space-y-32 mb-32">
        <div
          v-for="(tour, index) in sortedTours"
          :key="tour.id"
          class="group relative flex flex-col lg:flex-row gap-12 items-center"
          :class="{ 'lg:flex-row-reverse': index % 2 !== 0 }"
        >
          <!-- Image Section -->
          <div class="w-full lg:w-1/2 relative">
            <div class="relative aspect-[4/3] rounded-2xl overflow-hidden atacama-card group-hover:shadow-2xl group-hover:shadow-primary-500/20 transition-all duration-700">
              <img
                :src="getTourImage(tour)"
                :alt="getTourName(tour)"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-neutral-950/80 via-transparent to-transparent opacity-60" />
              
              <!-- Floating Price Badge -->
              <div class="absolute bottom-6 right-6 backdrop-blur-md bg-neutral-900/80 border border-white/10 px-6 py-3 rounded-full">
                <span class="text-sm text-neutral-400 uppercase tracking-wider mr-2">{{ t('tours.price_from') }}</span>
                <span class="text-xl font-bold text-white">{{ formatPrice(tour.price) }}</span>
              </div>
            </div>
            
            <!-- Decorative Elements behind image -->
            <div 
              class="absolute -z-10 w-full h-full border border-primary/30 rounded-2xl top-4 transition-transform duration-500"
              :class="index % 2 === 0 ? 'left-4 group-hover:translate-x-2 group-hover:translate-y-2' : 'right-4 group-hover:-translate-x-2 group-hover:translate-y-2'"
            />
          </div>

          <!-- Content Section -->
          <div class="w-full lg:w-1/2 space-y-6">
            <div class="flex items-center gap-3 mb-2">
              <UBadge
                color="primary"
                variant="soft"
                class="uppercase tracking-widest"
              >
                {{ t(`tours.category.${tour.category}`, tour.category) }}
              </UBadge>
              <div v-if="tour.moonSensitive" class="flex items-center gap-1 text-xs text-tertiary-400">
                <UIcon name="i-lucide-moon" class="w-3 h-3" />
                <span>{{ t('tours.sensitive.moon') }}</span>
              </div>
            </div>

            <h2 class="text-4xl md:text-5xl font-display font-bold text-white text-glow leading-tight">
              {{ getTourName(tour) }}
            </h2>

            <p class="text-lg text-neutral-200 leading-relaxed line-clamp-4">
              {{ getTourDescription(tour) }}
            </p>

            <div class="flex flex-wrap gap-6 text-neutral-400 py-4 border-y border-white/10">
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-clock" class="w-5 h-5 text-primary" />
                <span>{{ t('tours.duration_hours', { hours: tour.durationHours }) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-users" class="w-5 h-5 text-primary" />
                <span>{{ t('tours.max_participants', { count: (tour as any).maxParticipants }) }}</span>
              </div>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-map-pin" class="w-5 h-5 text-primary" />
                <span>San Pedro de Atacama</span>
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
                {{ t('tours.view_details') || 'Ver Detalles' }}
              </UButton>
            </div>
          </div>
        </div>
      </div>

      <!-- Calendar Section -->
      <div class="mt-32">
        <div class="text-center mb-12">
          <h2 class="text-3xl md:text-4xl font-display font-bold text-white mb-4 text-glow">
            {{ t("tours.calendar_title") || "Calendario de Disponibilidad" }}
          </h2>
          <p class="text-neutral-400">
            Planifica tu visita. Revisa las fechas disponibles para todas nuestras experiencias.
          </p>
        </div>

        <TourCalendar
          :tours="sortedTours"
          @schedule-click="handleScheduleClick"
          class="atacama-card rounded-xl overflow-hidden shadow-2xl"
        >
          <template #info>
            <div class="mt-4 p-4 bg-info/10 rounded-lg border border-info/20 flex items-start gap-3">
              <UIcon
                name="i-lucide-info"
                class="w-5 h-5 text-info-400 mt-0.5"
              />
              <p class="text-sm text-info-200">
                {{ t("tours.calendar.info_text") || "Haz clic en un evento para reservar. Los cupos son limitados para garantizar una experiencia íntima y personalizada." }}
              </p>
            </div>
          </template>
        </TourCalendar>
      </div>
    </UContainer>
  </div>
</template>
