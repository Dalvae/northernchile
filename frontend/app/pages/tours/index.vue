<script setup lang="ts">
import type { TourRes } from '~/lib/api-client'
import TourCard from '~/components/tour/TourCard.vue'

const router = useRouter()
const { t } = useI18n()

const { fetchAll } = useTours()
const { data: allTours } = await fetchAll()

// Filtros
const searchQuery = ref('')
const selectedCategory = ref<string | null>(null)
const sortBy = ref<'name' | 'price-asc' | 'price-desc' | 'duration'>('name')

// Tours filtrados y ordenados
const filteredTours = computed(() => {
  let tours = (allTours.value || []).filter(t => t.status === 'PUBLISHED')

  // Filtrar por búsqueda
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    tours = tours.filter((t) => {
      const name = (t.nameTranslations?.es || '').toLowerCase()
      const description = (t.descriptionTranslations?.es || '').toLowerCase()
      return name.includes(query) || description.includes(query)
    })
  }

  // Filtrar por categoría
  if (selectedCategory.value) {
    tours = tours.filter(t => t.category === selectedCategory.value)
  }

  // Ordenar
  if (sortBy.value === 'price-asc') {
    tours = tours.sort((a, b) => (a.basePrice || 0) - (b.basePrice || 0))
  } else if (sortBy.value === 'price-desc') {
    tours = tours.sort((a, b) => (b.basePrice || 0) - (a.basePrice || 0))
  } else if (sortBy.value === 'duration') {
    tours = tours.sort((a, b) => (a.durationHours || 0) - (b.durationHours || 0))
  } else {
    tours = tours.sort((a, b) =>
      (a.nameTranslations?.es || '').localeCompare(b.nameTranslations?.es || '')
    )
  }

  return tours
})

// Paginación
const page = ref(1)
const pageSize = 9
const totalPages = computed(() =>
  Math.ceil(filteredTours.value.length / pageSize)
)
const paginatedTours = computed(() => {
  const start = (page.value - 1) * pageSize
  const end = start + pageSize
  return filteredTours.value.slice(start, end)
})

// Reset página cuando cambian los filtros
watch([searchQuery, selectedCategory, sortBy], () => {
  page.value = 1
})

// Handle calendar schedule click
function handleScheduleClick(schedule: any, tour: any) {
  if (tour?.slug) {
    router.push(`/tours/${tour.slug}/schedule`)
  }
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
  <div class="min-h-screen bg-white dark:bg-neutral-900">
    <UContainer class="py-8 sm:py-12">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold text-neutral-900 dark:text-white mb-4">
          {{ t("tours.all") }}
        </h1>
        <p class="text-lg text-neutral-600 dark:text-neutral-400">
          Descubre todas nuestras experiencias bajo las estrellas del Atacama
        </p>
      </div>

      <!-- Filters Bar -->
      <div class="mb-8">
        <TourFilters
          v-model:search-query="searchQuery"
          v-model:selected-category="selectedCategory"
          v-model:sort-by="sortBy"
          :results-count="filteredTours.length"
        />
      </div>

      <!-- Tours Grid -->
      <div
        v-if="paginatedTours.length > 0"
        class="space-y-8 mb-12"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <TourCard
            v-for="tour in paginatedTours"
            :key="tour.id"
            :tour="tour"
            variant="list"
          />
        </div>

        <!-- Paginación -->
        <div
          v-if="totalPages > 1"
          class="flex justify-center"
        >
          <UPagination
            v-model="page"
            :total="filteredTours.length"
            :page-size="pageSize"
            show-first
            show-last
          />
        </div>
      </div>

      <!-- Empty State -->
      <UCard
        v-else
        class="text-center py-12 mb-12"
      >
        <div class="space-y-4">
          <div
            class="w-16 h-16 mx-auto rounded-full bg-neutral-100 dark:bg-neutral-800 flex items-center justify-center"
          >
            <UIcon
              name="i-lucide-telescope"
              class="w-8 h-8 text-neutral-400"
            />
          </div>
          <div>
            <h3
              class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
            >
              {{ t("tours.no_results") }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-400">
              Intenta ajustar los filtros o realizar una nueva búsqueda
            </p>
          </div>
          <UButton
            v-if="searchQuery || selectedCategory"
            color="primary"
            variant="outline"
            icon="i-lucide-refresh-cw"
            @click="
              searchQuery = '';
              selectedCategory = null;
            "
          >
            {{ t("tours.clear_filters") }}
          </UButton>
        </div>
      </UCard>

      <!-- Calendar Section (AFTER tours list) -->
      <div>
        <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-6">
          {{ t("tours.calendar_title") || "Calendario de Disponibilidad" }}
        </h2>

        <TourCalendar
          :tours="filteredTours"
          @schedule-click="handleScheduleClick"
        >
          <template #info>
            <div class="mt-4 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <p class="text-sm text-blue-900 dark:text-blue-200">
                <UIcon
                  name="i-lucide-info"
                  class="inline w-4 h-4 mr-1"
                />
                {{ t("tours.calendar.info_text") || "Haz clic en un evento para reservar. Los filtros afectan qué tours se muestran en el calendario." }}
              </p>
            </div>
          </template>
        </TourCalendar>
      </div>
    </UContainer>
  </div>
</template>
