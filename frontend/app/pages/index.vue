<script setup lang="ts">
const { fetchAll } = useTours()
const { data: tours, pending, error } = await fetchAll()
const { t } = useI18n()

// SEO específico para la página de inicio
useSeoMeta({
  title: () => t('nav.tours'), // Esto se insertará en el '%s' de la plantilla
  description: () => t('tours.hero_description'),
  ogTitle: () => `${t('nav.tours')} | Northern Chile`,
  ogDescription: () => t('tours.hero_description'),
  ogImage: 'https://www.northernchile.cl/og-image-homepage.jpg', // URL a una imagen representativa
  twitterCard: 'summary_large_image'
})
</script>

<template>
  <UContainer>
    <UPageHero
      :title="$t('tours.all')"
      :description="$t('tours.hero_description')"
    />

    <div v-if="pending">
      <p>Cargando tours...</p>
    </div>
    <div v-else-if="error">
      <p>Ocurrió un error al cargar los tours: {{ error.message }}</p>
    </div>
    <div v-else-if="tours && tours.data" class="grid grid-cols-1 md:grid-cols-3 gap-8 mt-8">
      <!-- Asumiendo que `tours.data` contiene el array -->
      <TourCard v-for="tour in tours.data" :key="tour.id" :tour="tour" />
    </div>
    <div v-else>
      <p>No se encontraron tours.</p>
    </div>
  </UContainer>
</template>