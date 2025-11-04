<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TourRes } from '~/lib/api-client'

const route = useRoute()
const { locale } = useI18n()
const tourSlug = route.params.slug as string

const { data: tour, pending: tourPending, error: tourError } = await useFetch<TourRes>(`/api/tours/slug/${tourSlug}`)

// Ref para el contenido enriquecido
const tourContent = ref<any>(null)

// Función para cargar dinámicamente el contenido
async function fetchContent(contentKey: string | undefined) {
  if (!contentKey) {
    tourContent.value = null
    return
  }
  try {
    // Importación dinámica basada en la clave
    const contentModule = await import(`~/app/content/tours/${contentKey}.ts`)
    tourContent.value = contentModule.default
  } catch (e) {
    console.error("No se encontró contenido enriquecido para la clave:", contentKey)
    tourContent.value = null
  }
}

// Carga el contenido cuando los datos del tour estén disponibles
watch(tour, (newTour) => {
  if (newTour?.contentKey) {
    fetchContent(newTour.contentKey)
  }
}, { immediate: true })

// Propiedades computadas para usar en el template
const translatedContent = computed(() => {
  if (!tourContent.value) return null
  return tourContent.value[locale.value] || tourContent.value.es
})

const translatedName = computed(() => tour.value?.nameTranslations?.[locale.value] || tour.value?.nameTranslations?.es || '')
const translatedDescription = computed(() => tour.value?.descriptionTranslations?.[locale.value] || tour.value?.descriptionTranslations?.es || '')
const heroImage = computed(() => tour.value?.images?.[0]?.imageUrl || 'default-image.jpg')

async function goToSchedule() {
  console.log('Navigating to schedule page for:', tourSlug)
  const schedulePath = `/tours/${tourSlug}/schedule`
  console.log('Schedule path:', schedulePath)
  await navigateTo(schedulePath)
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-900">
    <UContainer class="py-8">
      <div v-if="tourPending" class="text-center text-neutral-600 dark:text-neutral-400">Cargando tour...</div>
      <div v-else-if="tourError" class="text-center">
        <UAlert color="error" :title="tourError.message" />
      </div>
      <div v-else-if="tour" class="space-y-8">
        <!-- Hero Section -->
        <div>
          <h1 class="text-4xl font-bold mb-4 text-neutral-900 dark:text-white">{{ translatedName }}</h1>
          <img :src="heroImage" :alt="translatedName" class="w-full h-96 object-cover rounded-lg shadow-lg" />
        </div>

        <!-- Description -->
        <div class="prose dark:prose-invert max-w-none">
          <p class="text-lg text-neutral-600 dark:text-neutral-400">{{ translatedDescription }}</p>
        </div>

        <!-- Renderizar el contenido ENRIQUECIDO si existe -->
        <div v-if="translatedContent" class="space-y-8 mt-8">
    
          <!-- Sección del Guía -->
          <UCard>
            <template #header>
              <h2 class="text-2xl font-bold">Tu Guía: {{ translatedContent.guide.name }}</h2>
            </template>
            <p>{{ translatedContent.guide.bio }}</p>
            <ul class="mt-4 space-y-1 list-disc list-inside">
              <li v-for="cred in translatedContent.guide.credentials" :key="cred">{{ cred }}</li>
            </ul>
          </UCard>
          
          <!-- Sección del Itinerario -->
          <UCard>
            <template #header>
              <h2 class="text-2xl font-bold">Itinerario de tu Noche Cósmica</h2>
            </template>
            <div v-for="item in translatedContent.itinerary" :key="item.time" class="flex gap-4 py-2 border-b border-neutral-200 dark:border-neutral-700 last:border-b-0">
              <span class="font-bold w-20 text-primary">{{ item.time }}</span>
              <p>{{ item.description }}</p>
            </div>
          </UCard>
          
          <!-- Sección de Equipamiento -->
          <UCard>
            <template #header>
              <h2 class="text-2xl font-bold">Nuestro Equipo Técnico</h2>
            </template>
            <ul class="list-disc list-inside">
              <li v-for="item in translatedContent.equipment" :key="item">{{ item }}</li>
            </ul>
          </UCard>

           <!-- Sección de Incluye -->
          <UCard>
            <template #header>
              <h2 class="text-2xl font-bold">Incluye</h2>
            </template>
            <ul class="list-disc list-inside">
              <li v-for="item in translatedContent.includes" :key="item">{{ item }}</li>
            </ul>
          </UCard>
        </div>

        <!-- Gallery -->
        <div v-if="tour.images && tour.images.length > 1">
          <h2 class="text-2xl font-semibold mb-4 text-neutral-900 dark:text-white">Galería</h2>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            <img
              v-for="(image, index) in tour.images"
              :key="image.id || index"
              :src="image.imageUrl"
              :alt="image.altTextTranslations?.[locale] || image.altTextTranslations?.['es'] || translatedName + ' image ' + index"
              class="w-full h-32 object-cover rounded-lg shadow-md hover:shadow-xl transition-shadow"
            />
          </div>
        </div>

        <!-- Tour Details Card -->
        <UCard>
          <template #header>
            <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white">Detalles del Tour</h2>
          </template>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-tag" class="w-5 h-5 text-primary" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Categoría</p>
                <p class="font-medium text-neutral-900 dark:text-white">{{ tour.category }}</p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-dollar-sign" class="w-5 h-5 text-primary" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Precio</p>
                <p class="font-medium text-neutral-900 dark:text-white">${{ tour.price }}</p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-clock" class="w-5 h-5 text-primary" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Duración</p>
                <p class="font-medium text-neutral-900 dark:text-white">{{ tour.durationHours }} horas</p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-users" class="w-5 h-5 text-primary" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Máximo Participantes</p>
                <p class="font-medium text-neutral-900 dark:text-white">{{ tour.defaultMaxParticipants }}</p>
              </div>
            </div>

            <div v-if="tour.isWindSensitive" class="flex items-center gap-3">
              <UIcon name="i-lucide-wind" class="w-5 h-5 text-info" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Sensible al Viento</p>
                <UBadge color="info" size="sm">Sí</UBadge>
              </div>
            </div>

            <div v-if="tour.isMoonSensitive" class="flex items-center gap-3">
              <UIcon name="i-lucide-moon" class="w-5 h-5 text-tertiary" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Sensible a la Luna</p>
                <UBadge color="tertiary" size="sm">Sí</UBadge>
              </div>
            </div>

            <div v-if="tour.isCloudSensitive" class="flex items-center gap-3">
              <UIcon name="i-lucide-cloud" class="w-5 h-5 text-neutral" />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">Sensible a la Nubosidad</p>
                <UBadge color="neutral" size="sm">Sí</UBadge>
              </div>
            </div>
          </div>

          <template #footer>
            <div class="flex justify-between items-center">
              <UButton
                to="/tours"
                color="neutral"
                variant="ghost"
                icon="i-lucide-arrow-left"
              >
                Volver a Tours
              </UButton>
              <UButton
                color="primary"
                size="lg"
                icon="i-lucide-calendar"
                trailing
                @click="goToSchedule"
              >
                Ver Horarios Disponibles
              </UButton>
            </div>
          </template>
        </UCard>
      </div>
      <div v-else class="text-center">
        <UAlert color="warning" title="Tour no encontrado" />
      </div>
    </UContainer>
  </div>
</template>
