<script setup lang="ts">
const { fetchAll } = useTours()
const { data: toursData } = await fetchAll()

// Obtener tours destacados (primeros 6 publicados)
const featuredTours = computed(() => {
  const published = (toursData.value || []).filter(
    t => t.status === 'PUBLISHED'
  )
  return published.slice(0, 6)
})

// Características de la empresa
const features = [
  {
    icon: 'i-lucide-telescope',
    title: 'Equipamiento Profesional',
    description:
      'Telescopios de alta gama y equipos de observación certificados'
  },
  {
    icon: 'i-lucide-users',
    title: 'Guías Expertos',
    description: 'Astrónomos y guías certificados con años de experiencia'
  },
  {
    icon: 'i-lucide-star',
    title: 'Cielos Únicos',
    description: 'El mejor lugar del mundo para observación astronómica'
  },
  {
    icon: 'i-lucide-shield-check',
    title: 'Cancelación Gratis',
    description: 'Cancela sin cargo hasta 24 horas antes'
  }
]

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
    <!-- Hero Section -->
    <HomeHeroSection />

    <!-- Tours Destacados -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-neutral-900 dark:text-white">
            Tours Destacados
          </h2>
          <p class="mt-4 text-lg text-neutral-600 dark:text-neutral-400">
            Explora nuestras experiencias más populares bajo las estrellas
          </p>
        </div>

        <div
          v-if="featuredTours.length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          <HomeTourCard
            v-for="tour in featuredTours"
            :key="tour.id"
            :tour="tour"
          />
        </div>

        <div
          v-else
          class="text-center text-neutral-600 dark:text-neutral-400 py-8"
        >
          <UIcon
            name="i-lucide-telescope"
            class="w-16 h-16 mx-auto mb-4 text-neutral-400"
          />
          <p>Estamos preparando nuevas aventuras bajo las estrellas</p>
        </div>

        <!-- CTA para ver todos -->
        <div class="text-center mt-12">
          <UButton
            to="/tours"
            size="xl"
            color="primary"
            icon="i-lucide-arrow-right"
            trailing
          >
            Ver Todos los Tours
          </UButton>
        </div>
      </UContainer>
    </section>

    <!-- Features - ¿Por qué elegirnos? -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <div class="text-center mb-12">
          <h2 class="text-3xl font-bold text-neutral-900 dark:text-white">
            ¿Por Qué Elegirnos?
          </h2>
          <p class="mt-4 text-lg text-neutral-600 dark:text-neutral-400">
            La mejor experiencia astronómica en el desierto de Atacama
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <UCard
            v-for="feature in features"
            :key="feature.title"
            class="text-center"
          >
            <div class="space-y-4">
              <div
                class="mx-auto w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center"
              >
                <UIcon
                  :name="feature.icon"
                  class="w-8 h-8 text-primary"
                />
              </div>
              <h3
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ feature.title }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400">
                {{ feature.description }}
              </p>
            </div>
          </UCard>
        </div>
      </UContainer>
    </section>

    <!-- Call to Action Final -->
    <section
      class="py-20 bg-gradient-to-b from-neutral-900 to-neutral-950 text-white"
    >
      <UContainer>
        <div class="max-w-3xl mx-auto text-center space-y-6">
          <h2 class="text-4xl font-bold">
            ¿Listo para Explorar el Universo?
          </h2>
          <p class="text-xl text-neutral-300">
            Reserva tu tour astronómico y vive una experiencia inolvidable bajo
            las estrellas del Atacama
          </p>
          <div class="flex flex-col sm:flex-row gap-4 justify-center pt-4">
            <UButton
              to="/tours"
              size="xl"
              color="primary"
              icon="i-lucide-calendar"
            >
              Reservar Ahora
            </UButton>
            <UButton
              to="/contact"
              size="xl"
              color="neutral"
              variant="outline"
              icon="i-lucide-help-circle"
            >
              ¿Tienes Dudas?
            </UButton>
          </div>
        </div>
      </UContainer>
    </section>
  </div>
</template>
