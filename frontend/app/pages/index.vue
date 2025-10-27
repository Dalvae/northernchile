<script setup lang="ts">
const { fetchAll } = useTours()
const { data: toursData, pending, error } = await fetchAll()
const tours = computed(() => toursData.value || [])
const { t } = useI18n()

useSeoMeta({
  title: () => t('nav.tours'),
  description: () => t('tours.hero_description'),
  ogTitle: () => `${t('nav.tours')} | Northern Chile`,
  ogDescription: () => t('tours.hero_description'),
  ogImage: 'https://www.northernchile.cl/og-image-homepage.jpg',
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <div>
    <UContainer>
      <div class="py-16">
        <div class="text-center mb-12">
          <h1 class="text-4xl font-bold font-display tracking-tight sm:text-6xl">{{ $t('tours.all') }}</h1>
          <p class="mt-6 text-lg leading-8 text-gray-300">{{ $t('tours.hero_description') }}</p>
        </div>

        <div v-if="pending" class="text-center">
          <p>Cargando experiencias...</p>
        </div>
        <div v-else-if="error" class="text-center text-red-400">
          <p>Ocurrió un error al cargar los tours. Por favor, intenta más tarde.</p>
        </div>
        <div v-else-if="tours.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <TourCard v-for="tour in tours" :key="tour.id" :tour="tour" />
        </div>
        <div v-else class="text-center bg-gray-800/50 p-8 rounded-lg border border-gray-700 max-w-md mx-auto">
          <UIcon name="i-lucide-telescope" class="text-4xl text-primary-400 mb-4" />
          <p class="font-semibold text-lg">No se encontraron tours. </p>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-2">Estamos preparando nuevas aventuras bajo las estrellas. ¡Vuelve pronto!</p>
        </div>
      </div>
    </UContainer>
  </div>
</template>