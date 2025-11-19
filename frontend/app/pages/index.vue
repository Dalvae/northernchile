<script setup lang="ts">
import TourCard from '~/components/tour/TourCard.vue'

const { fetchAll } = useTours()
const { data: toursData } = await fetchAll()

// Obtener tours destacados (primeros 6 publicados)
const featuredTours = computed(() => {
  const published = (toursData.value || []).filter(
    t => t.status === 'PUBLISHED'
  )
  return published.slice(0, 6)
})

// SEO
const { t } = useI18n()
useSeoMeta({
  title: 'Northern Chile - Tours Astronómicos en San Pedro de Atacama',
  description:
    'Descubre las estrellas del desierto de Atacama con nuestros tours astronómicos guiados. Experiencias únicas bajo el cielo más claro del mundo.',
  ogTitle: 'Northern Chile - Tours Astronómicos en San Pedro de Atacama',
  ogDescription:
    'Descubre las estrellas del desierto de Atacama con nuestros tours astronómicos guiados',
  ogImage: 'https://www.northernchile.cl/og-image-homepage.jpg',
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <div>
    <!-- Hero Section - CRITICAL: In initial viewport -->
    <HomeHeroSection />

    <!-- Experience Section - Lazy loaded (below fold) -->
    <LazyHomeExperience />

    <!-- Tours Destacados -->
    <section class="relative py-24 z-10 overflow-hidden">
      <!-- Background Gradient -->
      <div class="absolute inset-0 bg-gradient-to-b from-neutral-950 to-neutral-900 z-0" />

      <UContainer class="relative z-10">
        <div class="text-center mb-16">
          <h2 class="text-4xl md:text-5xl font-display font-bold text-white mb-4 text-glow">
            {{ t("home.featured_tours_title") || "Experiencias Destacadas" }}
          </h2>
          <p class="text-xl text-neutral-200 max-w-2xl mx-auto">
            {{ t("home.featured_tours_subtitle") || "Descubre lo mejor del desierto de Atacama" }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <TourCard
            v-for="tour in featuredTours"
            :key="tour.id"
            :tour="tour"
            class="h-full"
          />
        </div>

        <div class="mt-16 text-center">
          <UButton
            to="/tours"
            size="xl"
            variant="ghost"
            color="primary"
            class="group text-lg text-neutral-200 hover:text-white"
            trailing-icon="i-lucide-arrow-right"
          >
            {{ t("home.view_all_tours") || "Ver todos los tours" }}
          </UButton>
        </div>
      </UContainer>
    </section>

    <!-- Why Choose Us Section - Lazy loaded (below fold) -->
    <LazyHomeWhyChooseUs />

    <!-- Call to Action -->
    <section class="relative py-32 z-10">
      <div class="absolute inset-0 atacama-gradient opacity-80 z-0" />
      <UContainer class="relative z-10 text-center">
        <h2 class="text-4xl md:text-6xl font-display font-bold text-white mb-8 text-glow">
          {{ t("home.cta_title") }}
        </h2>
        <p class="text-xl md:text-2xl text-neutral-200 mb-12 max-w-3xl mx-auto">
          {{ t("home.cta_subtitle") }}
        </p>
        <div class="flex flex-col sm:flex-row gap-6 justify-center">
          <UButton
            to="/tours"
            size="xl"
            color="primary"
            variant="solid"
            class="px-12 py-4 text-lg font-bold cobre-glow hover:scale-105 transition-transform"
          >
            {{ t("home.book_now") }}
          </UButton>
          <UButton
            to="/contact"
            size="xl"
            variant="outline"
            color="white"
            class="px-12 py-4 text-lg backdrop-blur-sm hover:bg-white/10"
          >
            {{ t("home.contact_us") }}
          </UButton>
        </div>
      </UContainer>
    </section>
  </div>
</template>
