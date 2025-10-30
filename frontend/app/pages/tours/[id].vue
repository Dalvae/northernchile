<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { TourRes } from '~/lib/api-client'

const route = useRoute()
const { locale } = useI18n()
const tourId = route.params.id as string

const tour = ref<TourRes | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const translatedName = computed(() => 
  tour.value?.nameTranslations?.[locale.value] || tour.value?.nameTranslations?.['es'] || ''
)

const translatedDescription = computed(() => 
  tour.value?.descriptionTranslations?.[locale.value] || tour.value?.descriptionTranslations?.['es'] || ''
)

const heroImage = computed(() => 
  tour.value?.images?.find(img => img.isHeroImage)?.imageUrl || tour.value?.images?.[0]?.imageUrl || 'https://source.unsplash.com/random/1200x800?desert,stars'
)

async function fetchTour() {
  try {
    loading.value = true
    const response = await $fetch(`/api/tours/${tourId}`)
    tour.value = response as TourRes
  } catch (e: any) {
    error.value = e.message || 'Error fetching tour'
  } finally {
    loading.value = false
  }
}

onMounted(fetchTour)
</script>

<template>
  <div class="container mx-auto p-4">
    <div v-if="loading" class="text-center text-neutral-100">Cargando tour...</div>
    <div v-else-if="error" class="text-center text-error-500">{{ error }}</div>
    <div v-else-if="tour">
      <h1 class="text-4xl font-bold mb-4 text-neutral-900 dark:text-white">{{ translatedName }}</h1>
      <img :src="heroImage" :alt="translatedName" class="w-full h-96 object-cover rounded-lg mb-6" />
      <p class="text-lg text-neutral-300 mb-6">{{ translatedDescription }}</p>

      <!-- Gallery -->
      <div v-if="tour.images && tour.images.length > 1" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-6">
        <img
          v-for="(image, index) in tour.images"
          :key="image.id || index"
          :src="image.imageUrl"
          :alt="image.altTextTranslations?.[locale] || image.altTextTranslations?.['es'] || translatedName + ' image ' + index"
          class="w-full h-32 object-cover rounded-lg shadow-md"
        />
      </div>

      <!-- Other tour details -->
      <div class="bg-neutral-800 p-6 rounded-lg shadow-md text-neutral-200">
        <h2 class="text-2xl font-semibold mb-4">Detalles del Tour</h2>
        <p><strong>Categoría:</strong> {{ tour.category }}</p>
        <p><strong>Precio Adulto:</strong> ${{ tour.priceAdult }}</p>
        <p v-if="tour.priceChild"><strong>Precio Niño:</strong> ${{ tour.priceChild }}</p>
        <p><strong>Duración:</strong> {{ tour.durationHours }} horas</p>
        <p><strong>Máximo Participantes:</strong> {{ tour.defaultMaxParticipants }}</p>
        <p><strong>Sensible al Viento:</strong> {{ tour.isWindSensitive ? 'Sí' : 'No' }}</p>
        <p><strong>Sensible a la Luna:</strong> {{ tour.isMoonSensitive ? 'Sí' : 'No' }}</p>
        <p><strong>Sensible a la Nubosidad:</strong> {{ tour.isCloudSensitive ? 'Sí' : 'No' }}</p>
      </div>
    </div>
    <div v-else class="text-center text-neutral-100">Tour no encontrado.</div>
  </div>
</template>
