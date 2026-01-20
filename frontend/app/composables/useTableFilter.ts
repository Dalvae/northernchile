/**
 * Composable for filtering and searching table data.
 *
 * @example
 * const { searchQuery, filterValue, filteredData, clearFilters } = useTableFilter(
 *   users,
 *   {
 *     searchFields: ['name', 'email'],
 *     filterField: 'role',
 *     defaultFilter: 'ALL'
 *   }
 * )
 */
export function useTableFilter<T extends object>(
  data: Ref<T[] | null | undefined> | ComputedRef<T[]>,
  options: {
    searchFields: (keyof T)[]
    filterField?: keyof T
    defaultFilter?: string
    caseSensitive?: boolean
  }
) {
  const searchQuery = ref('')
  const filterValue = ref(options.defaultFilter || 'ALL')

  const filteredData = computed(() => {
    let result = data.value || []

    // Apply search filter
    if (searchQuery.value.trim()) {
      const query = options.caseSensitive
        ? searchQuery.value.trim()
        : searchQuery.value.trim().toLowerCase()

      result = result.filter((item) => {
        return options.searchFields.some((field) => {
          const value = item[field]
          if (value == null) return false

          const stringValue = String(value)
          const compareValue = options.caseSensitive
            ? stringValue
            : stringValue.toLowerCase()

          return compareValue.includes(query)
        })
      })
    }

    // Apply status/category filter
    if (options.filterField && filterValue.value && filterValue.value !== 'ALL') {
      result = result.filter((item) => {
        return item[options.filterField!] === filterValue.value
      })
    }

    return result
  })

  /**
   * Clear all filters and search.
   */
  function clearFilters() {
    searchQuery.value = ''
    filterValue.value = options.defaultFilter || 'ALL'
  }

  /**
   * Check if any filters are active.
   */
  const hasActiveFilters = computed(() => {
    return searchQuery.value.trim() !== ''
      || (filterValue.value !== 'ALL' && filterValue.value !== options.defaultFilter)
  })

  return {
    searchQuery,
    filterValue,
    filteredData,
    clearFilters,
    hasActiveFilters
  }
}
