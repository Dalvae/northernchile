<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TourRes } from 'api-client'

const route = useRoute()
const { locale } = useI18n()
const tourSlug = route.params.slug as string

const {
  data: tour,
  pending: tourPending,
  error: tourError
} = await useFetch<TourRes>(`/api/tours/slug/${tourSlug}`)

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
    console.error(
      'No se encontró contenido enriquecido para la clave:',
      contentKey
    )
    tourContent.value = null
  }
}

// Carga el contenido cuando los datos del tour estén disponibles
watch(
  tour,
  (newTour) => {
    if (newTour?.contentKey) {
      fetchContent(newTour.contentKey)
    }
  },
  { immediate: true }
)

// Propiedades computadas para usar en el template
// Contenido enriquecido legacy (app/content/tours/*). Si existe, se usa.
const translatedContent = computed(() => {
  if (!tourContent.value) return null
  return tourContent.value[locale.value] || tourContent.value.es
})

// Bloques estructurados desde API (descriptionBlocksTranslations)
const descriptionBlocks = computed(() => {
  const blocks = (tour.value as any)?.descriptionBlocksTranslations?.[locale.value]
    || (tour.value as any)?.descriptionBlocksTranslations?.es
  return Array.isArray(blocks) ? blocks.filter((b: any) => b?.type && b?.content) : []
})

// Texto plano fallback si no hay bloques
const translatedDescription = computed(() => {
  if (descriptionBlocks.value.length) {
    return ''
  }
  return (tour.value as any)?.descriptionTranslations?.[locale.value]
    || (tour.value as any)?.descriptionTranslations?.es
    || ''
})

const translatedName = computed(
  () =>
    tour.value?.nameTranslations?.[locale.value]
    || tour.value?.nameTranslations?.es
    || ''
)
const heroImage = computed(
  () => tour.value?.images?.[0]?.imageUrl || 'default-image.jpg'
)

async function goToSchedule() {
  const schedulePath = `/tours/${tourSlug}/schedule`
  await navigateTo(schedulePath)
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800">
    <UContainer class="py-8">
      <div
        v-if="tourPending"
        class="text-center text-neutral-600 dark:text-neutral-400"
      >
        Cargando tour...
      </div>
      <div
        v-else-if="tourError"
        class="text-center"
      >
        <UAlert
          color="error"
          :title="tourError.message"
        />
      </div>
      <div
        v-else-if="tour"
        class="space-y-8"
      >
        <!-- Hero Section -->
        <div>
          <h1 class="text-4xl font-bold mb-4 text-neutral-900 dark:text-white">
            {{ translatedName }}
          </h1>
          <img
            :src="heroImage"
            :alt="translatedName"
            class="w-full h-96 object-cover rounded-lg shadow-lg"
          >
        </div>

        <!-- Description blocks from API -->
        <div
          v-if="descriptionBlocks.length"
          class="prose dark:prose-invert max-w-none space-y-4"
        >
             <component
               :is="block.type === 'heading' ? 'h2' : 'p'"
               v-for="(block, index) in descriptionBlocks"
               :key="index"
               class="text-lg text-neutral-900 dark:text-neutral-100"
             >
            {{ block.content }}
          </component>
        </div>

        <!-- Fallback plain description -->
        <div
          v-else-if="translatedDescription"
          class="prose dark:prose-invert max-w-none"
        >
          <p class="text-lg text-neutral-600 dark:text-neutral-400">
            {{ translatedDescription }}
          </p>
        </div>

         <!-- Itinerario desde API (TourRes.itinerary mapeado en backend) -->
         <div
           v-if="tour?.itinerary && tour.itinerary.length"
           class="space-y-4 mt-8"
         >
           <UCard>
             <template #header>
               <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
                 Itinerário
               </h2>
             </template>
             <div
               v-for="item in tour.itinerary"
               :key="item.time + item.description"
               class="flex items-center gap-4 py-2 border-b border-neutral-300 dark:border-neutral-700 last:border-b-0"
             >
               <span class="font-bold shrink-0 w-40 text-primary">
                 {{ item.time }}
               </span>
               <p class="text-neutral-900 dark:text-neutral-100">
                 {{ item.description }}
               </p>
             </div>
           </UCard>
         </div>

         <!-- Renderizar contenido ENRIQUECIDO legacy (contentKey) solo como fallback -->
         <div
           v-else-if="translatedContent"
           class="space-y-8 mt-8"
         >
           <UCard v-if="translatedContent.guide">
             <template #header>
               <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
                 Tu Guía
               </h2>
             </template>
             <p class="text-neutral-900 dark:text-neutral-100">
               {{ translatedContent.guide.bio }}
             </p>
             <ul class="mt-4 space-y-1 list-disc list-inside">
               <li
                 v-for="cred in translatedContent.guide.credentials"
                 :key="cred"
                 class="text-neutral-900 dark:text-neutral-100"
               >
                 {{ cred }}
               </li>
             </ul>
           </UCard>

           <UCard v-if="translatedContent.itinerary?.length">
             <template #header>
               <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
                 Itinerário
               </h2>
             </template>
             <div
               v-for="item in translatedContent.itinerary"
               :key="item.time + item.description"
               class="flex gap-4 py-2 border-b border-neutral-300 dark:border-neutral-700 last:border-b-0"
             >
               <span class="font-bold w-24 text-primary">
                 {{ item.time }}
               </span>
               <p class="text-neutral-900 dark:text-neutral-100">
                 {{ item.description }}
               </p>
             </div>
           </UCard>

           <UCard v-if="translatedContent.equipment?.length">
             <template #header>
               <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
                 Nosso Equipamento Técnico
               </h2>
             </template>
             <ul class="list-disc list-inside">
               <li
                 v-for="item in translatedContent.equipment"
                 :key="item"
                 class="text-neutral-900 dark:text-neutral-100"
               >
                 {{ item }}
               </li>
             </ul>
           </UCard>

           <UCard v-if="translatedContent.includes?.length">
             <template #header>
               <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
                 Inclui
               </h2>
             </template>
             <ul class="list-disc list-inside">
               <li
                 v-for="item in translatedContent.includes"
                 :key="item"
                 class="text-neutral-900 dark:text-neutral-100"
               >
                 {{ item }}
               </li>
             </ul>
           </UCard>
         </div>

        <!-- Gallery -->
        <div v-if="tour.images && tour.images.length > 1">
          <h2
            class="text-2xl font-semibold mb-4 text-neutral-900 dark:text-white"
          >
            Galería
          </h2>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            <img
              v-for="(image, index) in tour.images"
              :key="image.id || index"
              :src="image.imageUrl"
              :alt="
                image.altTextTranslations?.[locale]
                  || image.altTextTranslations?.['es']
                  || translatedName + ' image ' + index
              "
              class="w-full h-32 object-cover rounded-lg shadow-md hover:shadow-xl transition-shadow"
            >
          </div>
        </div>

        <!-- Tour Details Card -->
        <UCard>
          <template #header>
            <h2 class="text-2xl font-semibold text-neutral-900 dark:text-white">
              Detalles del Tour
            </h2>
          </template>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex items-center gap-3">
              <UIcon
                name="i-lucide-tag"
                class="w-5 h-5 text-primary"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Categoría
                </p>
                <p class="font-medium text-neutral-900 dark:text-white">
                  {{ tour.category }}
                </p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon
                name="i-lucide-dollar-sign"
                class="w-5 h-5 text-primary"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Precio
                </p>
                <p class="font-medium text-neutral-900 dark:text-white">
                  ${{ tour.price }}
                </p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon
                name="i-lucide-clock"
                class="w-5 h-5 text-primary"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Duración
                </p>
                <p class="font-medium text-neutral-900 dark:text-white">
                  {{ tour.durationHours }} horas
                </p>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <UIcon
                name="i-lucide-users"
                class="w-5 h-5 text-primary"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Máximo Participantes
                </p>
                <p class="font-medium text-neutral-900 dark:text-white">
                  {{ tour.defaultMaxParticipants }}
                </p>
              </div>
            </div>

            <div
              v-if="tour.isWindSensitive"
              class="flex items-center gap-3"
            >
              <UIcon
                name="i-lucide-wind"
                class="w-5 h-5 text-info"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Sensible al Viento
                </p>
                <UBadge
                  color="info"
                  size="sm"
                >
                  Sí
                </UBadge>
              </div>
            </div>

            <div
              v-if="tour.isMoonSensitive"
              class="flex items-center gap-3"
            >
              <UIcon
                name="i-lucide-moon"
                class="w-5 h-5 text-tertiary"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Sensible a la Luna
                </p>
                <UBadge
                  color="tertiary"
                  size="sm"
                >
                  Sí
                </UBadge>
              </div>
            </div>

            <div
              v-if="tour.isCloudSensitive"
              class="flex items-center gap-3"
            >
              <UIcon
                name="i-lucide-cloud"
                class="w-5 h-5 text-neutral"
              />
              <div>
                <p class="text-sm text-neutral-500 dark:text-neutral-400">
                  Sensible a la Nubosidad
                </p>
                <UBadge
                  color="neutral"
                  size="sm"
                >
                  Sí
                </UBadge>
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
      <div
        v-else
        class="text-center"
      >
        <UAlert
          color="warning"
          title="Tour no encontrado"
        />
      </div>
    </UContainer>
  </div>
</template>
