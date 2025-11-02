<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { TourRes } from '~/lib/api-client'

const route = useRoute()
const { locale } = useI18n()
const tourSlug = route.params.slug as string

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
    const response = await $fetch(`/api/tours/slug/${tourSlug}`)
    tour.value = response as TourRes
  } catch (e: any) {
    error.value = e.message || 'Error fetching tour ' + tourSlug
    console.error('Failed to fetch tour', tourSlug, e)
  } finally {
    loading.value = false
  }
}

async function goToSchedule() {
  console.log('Navigating to schedule page for:', tourSlug)
  const schedulePath = `/tours/${tourSlug}/schedule`
  console.log('Schedule path:', schedulePath)
  await navigateTo(schedulePath)
}

onMounted(fetchTour)
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-900">
    <UContainer class="py-8">
      <div v-if="loading" class="text-center text-neutral-600 dark:text-neutral-400">Cargando tour...</div>
      <div v-else-if="error" class="text-center">
        <UAlert color="error" :title="error" />
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
