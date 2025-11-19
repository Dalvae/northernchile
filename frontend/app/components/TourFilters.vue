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
  showResultsCount: true
})

const emit = defineEmits<{
  'update:searchQuery': [value: string]
  'update:selectedCategory': [value: string | null]
  'update:sortBy': [value: string]
  'clearFilters': []
}>()

const { t } = useI18n()

const localSearchQuery = computed({
  get: () => props.searchQuery,
  set: value => emit('update:searchQuery', value)
})

const localSelectedCategory = computed({
  get: () => props.selectedCategory,
  set: value => emit('update:selectedCategory', value)
})

const localSortBy = computed({
  get: () => props.sortBy,
  set: value => emit('update:sortBy', value)
})

const categoryOptions = computed(() => [
  { label: t('tours.all_categories'), value: null },
  { label: t('tours.category.ASTRONOMICAL'), value: 'ASTRONOMICAL' },
  { label: t('tours.category.REGULAR'), value: 'REGULAR' },
  { label: t('tours.category.SPECIAL'), value: 'SPECIAL' },
  { label: t('tours.category.PRIVATE'), value: 'PRIVATE' }
])

const sortOptions = computed(() => [
  { label: t('tours.sort_name'), value: 'name' },
  { label: t('tours.sort_price_asc'), value: 'price-asc' },
  { label: t('tours.sort_price_desc'), value: 'price-desc' },
  { label: t('tours.sort_duration'), value: 'duration' }
])

const hasActiveFilters = computed(() => {
  return props.searchQuery || props.selectedCategory
})

function clearFilters() {
  emit('update:searchQuery', '')
  emit('update:selectedCategory', null)
  emit('clearFilters')
}
</script>

<template>
  <div class="backdrop-blur-sm bg-white/5 rounded-xl p-6 border border-white/10">
    <div class="space-y-4">
      <!-- Primera fila: Búsqueda -->
      <div>
        <UInput
          v-model="localSearchQuery"
          icon="i-lucide-search"
          :placeholder="t('tours.search_placeholder')"
          size="xl"
          variant="ghost"
          class="w-full bg-white/5 text-white placeholder-neutral-400 focus:ring-primary-500"
          :ui="{ icon: { trailing: { pointer: '' } } }"
          aria-label="Buscar tours"
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
            size="xl"
            variant="ghost"
            class="w-full bg-white/5 text-white"
            aria-label="Filtrar por categoría"
          />
        </div>

        <div class="flex-1">
          <USelect
            v-model="localSortBy"
            :items="sortOptions"
            option-attribute="label"
            value-attribute="value"
            :placeholder="t('tours.sort_by')"
            size="xl"
            variant="ghost"
            class="w-full bg-white/5 text-white"
            aria-label="Ordenar tours"
          />
        </div>
      </div>

      <!-- Contador de resultados -->
      <div
        v-if="showResultsCount"
        class="flex items-center justify-between text-sm text-neutral-400 pt-2"
        role="status"
        aria-live="polite"
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
          class="hover:text-white"
          @click="clearFilters"
        >
          {{ t("tours.clear_filters") }}
        </UButton>
      </div>

      <!-- Slot for additional filters -->
      <slot name="additional-filters" />
    </div>
  </div>
</template>
