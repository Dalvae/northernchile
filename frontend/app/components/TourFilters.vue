<script setup lang="ts">
interface Props {
  searchQuery: string
  selectedCategory: string | null
  sortBy: string
  resultsCount?: number
  showResultsCount?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  resultsCount: 0,
  showResultsCount: true,
})

const emit = defineEmits<{
  "update:searchQuery": [value: string]
  "update:selectedCategory": [value: string | null]
  "update:sortBy": [value: string]
  "clearFilters": []
}>()

const { t } = useI18n()

const localSearchQuery = computed({
  get: () => props.searchQuery,
  set: (value) => emit("update:searchQuery", value),
})

const localSelectedCategory = computed({
  get: () => props.selectedCategory,
  set: (value) => emit("update:selectedCategory", value),
})

const localSortBy = computed({
  get: () => props.sortBy,
  set: (value) => emit("update:sortBy", value),
})

const categoryOptions = computed(() => [
  { label: t("tours.all_categories"), value: null },
  { label: t("tours.category.ASTRONOMICAL"), value: "ASTRONOMICAL" },
  { label: t("tours.category.REGULAR"), value: "REGULAR" },
  { label: t("tours.category.SPECIAL"), value: "SPECIAL" },
  { label: t("tours.category.PRIVATE"), value: "PRIVATE" },
])

const sortOptions = computed(() => [
  { label: t("tours.sort_name"), value: "name" },
  { label: t("tours.sort_price_asc"), value: "price-asc" },
  { label: t("tours.sort_price_desc"), value: "price-desc" },
  { label: t("tours.sort_duration"), value: "duration" },
])

const hasActiveFilters = computed(() => {
  return props.searchQuery || props.selectedCategory
})

function clearFilters() {
  emit("update:searchQuery", "")
  emit("update:selectedCategory", null)
  emit("clearFilters")
}
</script>

<template>
  <UCard>
    <div class="space-y-4">
      <!-- Primera fila: Búsqueda -->
      <div>
        <UInput
          v-model="localSearchQuery"
          icon="i-lucide-search"
          :placeholder="t('tours.search_placeholder')"
          size="lg"
          class="w-full"
        />
      </div>

      <!-- Segunda fila: Filtros y ordenamiento -->
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="flex-1">
          <USelect
            v-model="localSelectedCategory"
            :items="categoryOptions"
            option-attribute="label"
            value-attribute="value"
            :placeholder="t('tours.filter_by_category')"
            size="lg"
            class="w-full"
          />
        </div>

        <div class="flex-1">
          <USelect
            v-model="localSortBy"
            :items="sortOptions"
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
        v-if="showResultsCount"
        class="flex items-center justify-between text-sm text-neutral-600 dark:text-neutral-400"
      >
        <span>
          {{
            t(
              "tours.results",
              { count: resultsCount },
              resultsCount
            )
          }}
        </span>
        <UButton
          v-if="hasActiveFilters"
          color="neutral"
          variant="ghost"
          size="sm"
          icon="i-lucide-x"
          @click="clearFilters"
        >
          {{ t("tours.clear_filters") }}
        </UButton>
      </div>

      <!-- Slot for additional filters -->
      <slot name="additional-filters" />
    </div>
  </UCard>
</template>
