<script setup lang="ts">
import type { TourRes } from "~/lib/api-client";

const { fetchAll } = useTours();
const { data: allTours } = await fetchAll();

// Filtros
const searchQuery = ref("");
const selectedCategory = ref<string | null>(null);
const sortBy = ref<"name" | "price-asc" | "price-desc" | "duration">("name");

const { t } = useI18n();

// Opciones de filtros
const categoryOptions = computed(() => [
  { label: t("tours.all_categories"), value: null },
  { label: t("tours.category.ASTRONOMICAL"), value: "ASTRONOMICAL" },
  { label: t("tours.category.REGULAR"), value: "REGULAR" },
  { label: t("tours.category.SPECIAL"), value: "SPECIAL" },
  { label: t("tours.category.PRIVATE"), value: "PRIVATE" },
]);

const sortOptions = computed(() => [
  { label: t("tours.sort_name"), value: "name" },
  { label: t("tours.sort_price_asc"), value: "price-asc" },
  { label: t("tours.sort_price_desc"), value: "price-desc" },
  { label: t("tours.sort_duration"), value: "duration" },
]);

// Tours filtrados y ordenados
const filteredTours = computed(() => {
  let tours = (allTours.value || []).filter((t) => t.status === "PUBLISHED");

  // Filtrar por búsqueda
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    tours = tours.filter(
      (t) =>
        t.name?.toLowerCase().includes(query) ||
        t.description?.toLowerCase().includes(query)
    );
  }

  // Filtrar por categoría
  if (selectedCategory.value) {
    tours = tours.filter((t) => t.category === selectedCategory.value);
  }

  // Ordenar
  switch (sortBy.value) {
    case "name":
      tours.sort((a, b) => (a.name || "").localeCompare(b.name || ""));
      break;
    case "price-asc":
      tours.sort((a, b) => (a.price || 0) - (b.price || 0));
      break;
    case "price-desc":
      tours.sort((a, b) => (b.price || 0) - (a.price || 0));
      break;
    case "duration":
      tours.sort((a, b) => (a.durationHours || 0) - (b.durationHours || 0));
      break;
  }

  return tours;
});

// Paginación
const page = ref(1);
const pageSize = 9;
const totalPages = computed(() =>
  Math.ceil(filteredTours.value.length / pageSize)
);
const paginatedTours = computed(() => {
  const start = (page.value - 1) * pageSize;
  const end = start + pageSize;
  return filteredTours.value.slice(start, end);
});

// Reset página cuando cambian los filtros
watch([searchQuery, selectedCategory, sortBy], () => {
  page.value = 1;
});

// SEO
useSeoMeta({
  title: () => `${t("tours.all")} - Northern Chile`,
  description:
    "Explora nuestra selección completa de tours astronómicos y experiencias únicas en San Pedro de Atacama. Encuentra el tour perfecto para ti.",
  ogTitle: () => `${t("tours.all")} - Northern Chile`,
  ogDescription:
    "Explora nuestra selección completa de tours astronómicos y experiencias únicas en San Pedro de Atacama",
  ogImage: "https://www.northernchile.cl/og-image-tours.jpg",
  twitterCard: "summary_large_image",
});
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800">
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
      <UCard class="mb-8">
        <div class="space-y-4">
          <!-- Primera fila: Búsqueda -->
          <div>
            <UInput
              v-model="searchQuery"
              icon="i-lucide-search"
              :placeholder="t('tours.search_placeholder')"
              size="lg"
              class="w-full"
            />
          </div>

          <!-- Segunda fila: Filtros y ordenamiento -->
          <div class="flex flex-col sm:flex-row gap-4">
            <div class="flex-1">
              <USelectMenu
                v-model="selectedCategory"
                :options="categoryOptions"
                option-attribute="label"
                value-attribute="value"
                :placeholder="t('tours.filter_by_category')"
                size="lg"
                class="w-full"
              />
            </div>
            <div class="flex-1">
              <USelectMenu
                v-model="sortBy"
                :options="sortOptions"
                option-attribute="label"
                value-attribute="value"
                :placeholder="t('tours.sort_by')"
                size="lg"
                class="w-full"
              />
            </div>
          </div>

          <!-- Contador de resultados -->
          <div
            class="flex items-center justify-between text-sm text-neutral-600 dark:text-neutral-400"
          >
            <span>
              {{
                t(
                  "tours.results",
                  { count: filteredTours.length },
                  filteredTours.length
                )
              }}
            </span>
            <UButton
              v-if="searchQuery || selectedCategory"
              color="neutral"
              variant="ghost"
              size="sm"
              icon="i-lucide-x"
              @click="
                searchQuery = '';
                selectedCategory = null;
              "
            >
              {{ t("tours.clear_filters") }}
            </UButton>
          </div>
        </div>
      </UCard>

      <!-- Tours Grid -->
      <div v-if="paginatedTours.length > 0" class="space-y-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <HomeTourCard
            v-for="tour in paginatedTours"
            :key="tour.id"
            :tour="tour"
          />
        </div>

        <!-- Paginación -->
        <div v-if="totalPages > 1" class="flex justify-center">
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
      <UCard v-else class="text-center py-12">
        <div class="space-y-4">
          <div
            class="w-16 h-16 mx-auto rounded-full bg-neutral-100 dark:bg-neutral-800 flex items-center justify-center"
          >
            <UIcon name="i-lucide-telescope" class="w-8 h-8 text-neutral-400" />
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
    </UContainer>
  </div>
</template>
