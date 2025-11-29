<template>
  <div class="min-h-screen bg-default py-6">
    <UContainer>
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-default">
          Registro de Auditoría
        </h1>
        <p class="mt-1 text-sm text-muted">
          Historial completo de acciones administrativas
        </p>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-primary/10 rounded-lg">
              <UIcon
                name="i-lucide-file-text"
                class="w-6 h-6 text-primary"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Total
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ stats.totalLogs }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-success/10 rounded-lg">
              <UIcon
                name="i-lucide-plus-circle"
                class="w-6 h-6 text-success"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Creados
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ stats.createActions }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-warning/10 rounded-lg">
              <UIcon
                name="i-lucide-edit"
                class="w-6 h-6 text-warning"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Actualizados
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ stats.updateActions }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-error/10 rounded-lg">
              <UIcon
                name="i-lucide-trash-2"
                class="w-6 h-6 text-error"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Eliminados
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ stats.deleteActions }}
              </p>
            </div>
          </div>
        </UCard>
      </div>

      <!-- Filters -->
      <div class="bg-elevated rounded-lg shadow-sm p-4 mb-6 border border-default">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label
              class="block mb-2 text-sm font-medium text-default"
            >
              Acción
            </label>
            <USelect
              v-model="filters.action"
              :items="actionFilterOptions"
              option-attribute="label"
              value-attribute="value"
              size="lg"
              class="w-full"
              @change="resetPaginationAndFetch"
            />
          </div>

          <div>
            <label
              class="block mb-2 text-sm font-medium text-default"
            >
              Tipo de Entidad
            </label>
            <USelect
              v-model="filters.entityType"
              :items="entityTypeFilterOptions"
              option-attribute="label"
              value-attribute="value"
              size="lg"
              class="w-full"
              @change="resetPaginationAndFetch"
            />
          </div>

          <div>
            <label
              class="block mb-2 text-sm font-medium text-default"
            >
              Email Usuario
            </label>
            <UInput
              v-model="filters.userEmail"
              type="email"
              placeholder="admin@example.com"
              size="lg"
              class="w-full"
              @input="debouncedFetch"
            />
          </div>
        </div>
      </div>

      <!-- Audit Logs List -->
      <div
        v-if="pending"
        class="flex justify-center py-12"
      >
        <UIcon
          name="i-lucide-loader-2"
          class="w-8 h-8 animate-spin text-primary"
        />
      </div>

      <div
        v-else-if="!auditLogs || auditLogs.length === 0"
        class="text-center py-12 bg-elevated rounded-lg border border-default"
      >
        <UIcon
          name="i-lucide-file-search"
          class="w-16 h-16 mx-auto mb-4 text-muted"
        />
        <h3 class="mb-2 text-xl font-semibold text-default">
          No hay registros
        </h3>
        <p class="text-muted">
          No se encontraron registros de auditoría con los filtros seleccionados
        </p>
      </div>

      <div v-else>
        <!-- Timeline -->
        <div class="space-y-4">
          <UCard
            v-for="log in auditLogs"
            :key="log.id"
            class="hover:shadow-lg transition-shadow"
          >
            <div class="flex gap-4">
              <!-- Icon -->
              <div class="flex-shrink-0">
                <div
                  :class="['p-3 rounded-lg', getActionColorClass(log.action)]"
                >
                  <UIcon
                    :name="getActionIcon(log.action)"
                    class="w-5 h-5"
                  />
                </div>
              </div>

              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-4 mb-2">
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <UBadge
                        :color="getActionColor(log.action)"
                        :label="log.action"
                        size="sm"
                      />
                      <UBadge
                        color="neutral"
                        :label="log.entityType"
                        variant="outline"
                        size="sm"
                      />
                    </div>
                    <h3
                      class="text-lg font-semibold text-default"
                    >
                      {{
                        log.entityDescription
                          || `${log.entityType} ${log.entityId}`
                      }}
                    </h3>
                  </div>

                  <div class="text-right flex-shrink-0">
                    <p class="text-sm text-muted">
                      {{ formatDate(log.createdAt) }}
                    </p>
                    <p class="text-xs text-neutral-500 dark:text-neutral-500">
                      {{ formatTime(log.createdAt) }}
                    </p>
                  </div>
                </div>

                <div
                  class="grid md:grid-cols-2 gap-2 text-sm text-muted mb-3"
                >
                  <div class="flex items-center gap-2">
                    <UIcon
                      name="i-lucide-user"
                      class="w-4 h-4"
                    />
                    <span>{{ log.userEmail }}</span>
                    <UBadge
                      :label="log.userRole"
                      size="xs"
                      variant="subtle"
                    />
                  </div>

                  <div
                    v-if="log.ipAddress"
                    class="flex items-center gap-2"
                  >
                    <UIcon
                      name="i-lucide-globe"
                      class="w-4 h-4"
                    />
                    <span>{{ log.ipAddress }}</span>
                  </div>
                </div>

                <!-- Old/New Values -->
                <div
                  v-if="log.oldValues || log.newValues"
                  class="mt-3 pt-3 border-t border-default"
                >
                  <details class="group">
                    <summary
                      class="cursor-pointer text-sm font-medium text-default hover:text-primary transition-colors"
                    >
                      Ver detalles
                      <UIcon
                        name="i-lucide-chevron-down"
                        class="w-4 h-4 inline-block ml-1 transition-transform group-open:rotate-180"
                      />
                    </summary>

                    <div class="mt-3 space-y-3">
                      <div
                        v-if="
                          log.oldValues && Object.keys(log.oldValues).length > 0
                        "
                      >
                        <p
                          class="text-xs font-medium text-muted mb-1"
                        >
                          Valores Anteriores:
                        </p>
                        <pre
                          class="text-xs bg-muted p-3 rounded overflow-x-auto"
                        >{{ JSON.stringify(log.oldValues, null, 2) }}</pre>
                      </div>

                      <div
                        v-if="
                          log.newValues && Object.keys(log.newValues).length > 0
                        "
                      >
                        <p
                          class="text-xs font-medium text-muted mb-1"
                        >
                          Valores Nuevos:
                        </p>
                        <pre
                          class="text-xs bg-muted p-3 rounded overflow-x-auto"
                        >{{ JSON.stringify(log.newValues, null, 2) }}</pre>
                      </div>
                    </div>
                  </details>
                </div>
              </div>
            </div>
          </UCard>
        </div>

        <!-- Pagination -->
        <div
          class="flex items-center justify-between mt-6 bg-elevated rounded-lg p-4 border border-default"
        >
          <div class="text-sm text-muted">
            Mostrando {{ pagination.currentPage * pagination.pageSize + 1 }} -
            {{
              Math.min(
                (pagination.currentPage + 1) * pagination.pageSize,
                pagination.totalItems
              )
            }}
            de {{ pagination.totalItems }} registros
          </div>

          <div class="flex gap-2">
            <UButton
              icon="i-lucide-chevron-left"
              color="neutral"
              variant="outline"
              :disabled="pagination.currentPage === 0"
              @click="goToPage(pagination.currentPage - 1)"
            />

            <div class="flex gap-1">
              <UButton
                v-for="page in visiblePages"
                :key="page"
                :variant="page === pagination.currentPage ? 'solid' : 'outline'"
                :color="page === pagination.currentPage ? 'primary' : 'neutral'"
                size="sm"
                @click="goToPage(page)"
              >
                {{ page + 1 }}
              </UButton>
            </div>

            <UButton
              icon="i-lucide-chevron-right"
              color="neutral"
              variant="outline"
              :disabled="pagination.currentPage >= pagination.totalPages - 1"
              @click="goToPage(pagination.currentPage + 1)"
            />
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Auditoría - Admin - Northern Chile'
})

const { formatDate, formatTime } = useDateTime()

// State
const pending = ref(false)
const auditLogs = ref<any[]>([])
const stats = ref({
  totalLogs: 0,
  createActions: 0,
  updateActions: 0,
  deleteActions: 0
})

// Filters
const filters = ref({
  action: '',
  entityType: '',
  userEmail: ''
})

// Pagination
const pagination = ref({
  currentPage: 0,
  pageSize: 20,
  totalItems: 0,
  totalPages: 0
})

const actionFilterOptions = [
  { value: '', label: 'Todas' },
  { value: 'CREATE', label: 'Crear' },
  { value: 'UPDATE', label: 'Actualizar' },
  { value: 'DELETE', label: 'Eliminar' },
  { value: 'RESTORE', label: 'Restaurar' }
]

const entityTypeFilterOptions = [
  { value: '', label: 'Todos' },
  { value: 'TOUR', label: 'Tours' },
  { value: 'BOOKING', label: 'Reservas' },
  { value: 'USER', label: 'Usuarios' },
  { value: 'SCHEDULE', label: 'Schedules' },
  { value: 'ALERT', label: 'Alertas' }
]

// Fetch audit logs
const fetchAuditLogs = async () => {
  pending.value = true
  try {
    const params = new URLSearchParams({
      page: pagination.value.currentPage.toString(),
      size: pagination.value.pageSize.toString()
    })

    if (filters.value.action) params.append('action', filters.value.action)
    if (filters.value.entityType)
      params.append('entityType', filters.value.entityType)
    if (filters.value.userEmail)
      params.append('userEmail', filters.value.userEmail)

    const response = await $fetch<AuditLogsResponse>(
      `/api/admin/audit-logs?${params.toString()}`
    )

    auditLogs.value = response.data
    pagination.value.totalItems = response.totalItems
    pagination.value.totalPages = response.totalPages
    pagination.value.currentPage = response.currentPage
  } catch (error) {
    console.error('Error fetching audit logs:', error)
  } finally {
    pending.value = false
  }
}

// Fetch stats
const fetchStats = async () => {
  try {
    const response = await $fetch<AuditStatsResponse>(
      '/api/admin/audit-logs/stats'
    )
    stats.value = response
  } catch (error) {
    console.error('Error fetching stats:', error)
  }
}

// Debounced fetch for email filter
let debounceTimeout: ReturnType<typeof setTimeout> | null = null
const debouncedFetch = () => {
  if (debounceTimeout) clearTimeout(debounceTimeout)
  debounceTimeout = setTimeout(() => {
    resetPaginationAndFetch()
  }, 500)
}

// Reset pagination and fetch
const resetPaginationAndFetch = () => {
  pagination.value.currentPage = 0
  fetchAuditLogs()
}

// Go to page
const goToPage = (page: number) => {
  pagination.value.currentPage = page
  fetchAuditLogs()
}

// Computed visible pages for pagination
const visiblePages = computed(() => {
  const current = pagination.value.currentPage
  const total = pagination.value.totalPages
  const pages: number[] = []

  if (total <= 7) {
    for (let i = 0; i < total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      for (let i = 0; i < 5; i++) {
        pages.push(i)
      }
    } else if (current >= total - 4) {
      for (let i = total - 5; i < total; i++) {
        pages.push(i)
      }
    } else {
      for (let i = current - 2; i <= current + 2; i++) {
        pages.push(i)
      }
    }
  }

  return pages
})

// Helper functions
type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

interface AuditLogsResponse {
  data: any[]
  totalItems: number
  totalPages: number
  currentPage: number
}

interface AuditStatsResponse {
  totalLogs: number
  createActions: number
  updateActions: number
  deleteActions: number
}

function getActionColor(action: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'error',
    RESTORE: 'info'
  }
  return colors[action] || 'neutral'
}

function getActionColorClass(action: string): string {
  const classes: Record<string, string> = {
    CREATE: 'bg-success/10 text-success',
    UPDATE: 'bg-warning/10 text-warning',
    DELETE: 'bg-error/10 text-error',
    RESTORE: 'bg-info/10 text-info'
  }
  return classes[action] || 'bg-muted text-default'
}

function getActionIcon(action: string): string {
  const icons: Record<string, string> = {
    CREATE: 'i-lucide-plus-circle',
    UPDATE: 'i-lucide-edit',
    DELETE: 'i-lucide-trash-2',
    RESTORE: 'i-lucide-rotate-ccw'
  }
  return icons[action] || 'i-lucide-file'
}

// Initialize
onMounted(() => {
  fetchAuditLogs()
  fetchStats()
})
</script>
