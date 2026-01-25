<template>
  <div class="min-h-screen bg-default py-6">
    <UContainer>
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
        <div>
          <h1 class="text-3xl font-bold text-default">
            Alertas Climáticas
          </h1>
          <p class="mt-1 text-sm text-muted">
            Gestiona las alertas de condiciones climáticas adversas
          </p>
        </div>

        <div class="flex gap-2">
          <UButton
            color="neutral"
            variant="outline"
            icon="i-lucide-refresh-cw"
            :loading="refreshing"
            @click="() => refreshAlerts()"
          >
            Actualizar
          </UButton>
          <UButton
            color="primary"
            icon="i-lucide-cloud"
            :loading="checking"
            @click="checkAlertsManually"
          >
            Verificar Ahora
          </UButton>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-warning/10 rounded-lg">
              <UIcon
                name="i-lucide-alert-triangle"
                class="w-6 h-6 text-warning"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Pendientes
              </p>
              <p class="text-2xl font-bold text-default">
                {{ stats.pending }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-error/10 rounded-lg">
              <UIcon
                name="i-lucide-x-circle"
                class="w-6 h-6 text-error"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Cancelados
              </p>
              <p class="text-2xl font-bold text-default">
                {{ stats.cancelled }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-success/10 rounded-lg">
              <UIcon
                name="i-lucide-check-circle"
                class="w-6 h-6 text-success"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Mantenidos
              </p>
              <p class="text-2xl font-bold text-default">
                {{ stats.kept }}
              </p>
            </div>
          </div>
        </UCard>

        <UCard>
          <div class="flex items-center gap-3">
            <div class="p-2 bg-info/10 rounded-lg">
              <UIcon
                name="i-lucide-calendar"
                class="w-6 h-6 text-info"
              />
            </div>
            <div>
              <p class="text-sm text-muted">
                Reagendados
              </p>
              <p class="text-2xl font-bold text-default">
                {{ stats.rescheduled }}
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
              Estado
            </label>
            <USelect
              v-model="filters.status"
              :items="statusFilterOptions"
              option-attribute="label"
              value-attribute="value"
              size="lg"
              class="w-full"
            />
          </div>

          <div>
            <label
              class="block mb-2 text-sm font-medium text-default"
            >
              Severidad
            </label>
            <USelect
              v-model="filters.severity"
              :items="severityFilterOptions"
              option-attribute="label"
              value-attribute="value"
              size="lg"
              class="w-full"
            />
          </div>

          <div>
            <label
              class="block mb-2 text-sm font-medium text-default"
            >
              Tipo
            </label>
            <USelect
              v-model="filters.type"
              :items="typeFilterOptions"
              option-attribute="label"
              value-attribute="value"
              size="lg"
              class="w-full"
            />
          </div>
        </div>
      </div>

      <!-- Alerts List -->
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
        v-else-if="filteredAlerts.length === 0"
        class="text-center py-12 bg-elevated rounded-lg border border-default"
      >
        <UIcon
          name="i-lucide-check-circle"
          class="w-16 h-16 mx-auto mb-4 text-muted"
        />
        <h3 class="mb-2 text-xl font-semibold text-default">
          No hay alertas
        </h3>
        <p class="text-muted">
          No se encontraron alertas con los filtros seleccionados
        </p>
      </div>

      <div
        v-else
        class="grid gap-4"
      >
        <UCard
          v-for="alert in filteredAlerts"
          :key="alert.id"
          class="hover:shadow-lg transition-shadow"
        >
          <div
            class="flex flex-col md:flex-row md:items-start justify-between gap-4"
          >
            <!-- Alert Info -->
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <UBadge
                  :color="getSeverityColor(alert.severity || 'LOW')"
                  :label="alert.severity"
                  size="lg"
                />
                <UBadge
                  :color="getTypeColor(alert.alertType || 'WIND')"
                  :label="getTypeLabel(alert.alertType || 'WIND')"
                  variant="subtle"
                />
                <UBadge
                  :color="getStatusColor(alert.status || 'PENDING')"
                  :label="alert.status"
                  variant="outline"
                />
              </div>

              <h3
                class="mb-2 text-lg font-semibold text-default"
              >
                {{ alert.title }}
              </h3>

              <p class="text-sm text-muted mb-3">
                {{ alert.description }}
              </p>

              <div
                class="grid md:grid-cols-2 gap-2 text-sm text-muted"
              >
                <div class="flex items-center gap-2">
                  <UIcon
                    name="i-lucide-calendar"
                    class="w-4 h-4"
                  />
                  <span>{{ formatDate(alert.scheduleDate || '') }}</span>
                </div>

                <div class="flex items-center gap-2">
                  <UIcon
                    name="i-lucide-map-pin"
                    class="w-4 h-4"
                  />
                  <span>Tour
                    {{
                      alert.tourScheduleId ? "programado" : "no disponible"
                    }}</span>
                </div>

                <div
                  v-if="alert.windSpeed"
                  class="flex items-center gap-2"
                >
                  <UIcon
                    name="i-lucide-wind"
                    class="w-4 h-4"
                  />
                  <span>Viento: {{ alert.windSpeed }} nudos</span>
                </div>

                <div
                  v-if="alert.cloudCoverage"
                  class="flex items-center gap-2"
                >
                  <UIcon
                    name="i-lucide-cloud"
                    class="w-4 h-4"
                  />
                  <span>Nubes: {{ alert.cloudCoverage }}%</span>
                </div>

                <div
                  v-if="alert.moonIllumination !== null"
                  class="flex items-center gap-2"
                >
                  <UIcon
                    name="i-lucide-moon"
                    class="w-4 h-4"
                  />
                  <span>Luna: {{ alert.moonIllumination }}%</span>
                </div>

                <div class="flex items-center gap-2">
                  <UIcon
                    name="i-lucide-clock"
                    class="w-4 h-4"
                  />
                  <span>Creada: {{ formatDate(alert.createdAt || '') }}</span>
                </div>
              </div>

              <!-- Resolution info -->
              <div
                v-if="alert.status !== 'PENDING' && alert.resolvedAt"
                class="mt-3 pt-3 border-t border-default"
              >
                <p class="text-sm text-muted">
                  <strong>Resolución:</strong> {{ alert.resolution }}
                </p>
                <p class="mt-1 text-xs text-muted">
                  Resuelto el {{ formatDate(alert.resolvedAt) }}
                </p>
              </div>
            </div>

            <!-- Actions -->
            <div
              v-if="alert.status === 'PENDING'"
              class="flex flex-col gap-2"
            >
              <UButton
                color="primary"
                variant="outline"
                size="sm"
                icon="i-lucide-check"
                @click="openResolveModal(alert, 'KEPT')"
              >
                Mantener
              </UButton>
              <UButton
                color="error"
                variant="outline"
                size="sm"
                icon="i-lucide-x"
                @click="openResolveModal(alert, 'CANCELLED')"
              >
                Cancelar
              </UButton>
              <UButton
                color="warning"
                variant="outline"
                size="sm"
                icon="i-lucide-calendar"
                @click="openResolveModal(alert, 'RESCHEDULED')"
              >
                Reagendar
              </UButton>
            </div>
          </div>
        </UCard>
      </div>
    </UContainer>

    <!-- Resolve Alert Modal -->
    <UModal v-model:open="showResolveModal">
      <template #content>
        <div class="p-6">
          <!-- Header -->
          <div
            class="flex items-center justify-between pb-4 border-b border-default"
          >
            <h3 class="text-xl font-semibold text-default">
              Resolver Alerta
            </h3>
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              size="sm"
              @click="closeResolveModal"
            />
          </div>

          <!-- Content -->
          <div class="py-4 space-y-4">
            <div>
              <p
                class="mb-1 text-sm font-medium text-default"
              >
                Resolución seleccionada:
              </p>
              <UBadge
                :color="getResolutionColor(resolveForm.resolution)"
                :label="getResolutionLabel(resolveForm.resolution)"
                size="lg"
              />
            </div>

            <div v-if="selectedAlert">
              <p
                class="mb-1 text-sm font-medium text-default"
              >
                Alerta:
              </p>
              <p class="text-sm text-muted">
                {{ selectedAlert.title }}
              </p>
            </div>

            <div>
              <label
                class="block text-sm font-medium text-default mb-2"
              >
                Notas adicionales (opcional)
              </label>
              <UTextarea
                v-model="resolveForm.notes"
                :rows="3"
                placeholder="Agrega cualquier información adicional sobre la resolución..."
              />
            </div>
          </div>

          <!-- Footer -->
          <div
            class="flex justify-end gap-2 pt-4 border-t border-default"
          >
            <UButton
              color="neutral"
              variant="outline"
              @click="closeResolveModal"
            >
              Cancelar
            </UButton>
            <UButton
              :color="getResolutionColor(resolveForm.resolution)"
              :loading="resolving"
              @click="resolveAlert"
            >
              Confirmar Resolución
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { WeatherAlertRes, AlertHistoryRes } from 'api-client'
import { getStatusColor } from '~/utils/adminOptions'
import logger from '~/utils/logger'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Alertas Climáticas - Admin - Northern Chile'
})

const toast = useToast()
const { formatDateTime: formatDate } = useDateTime()

// State
const pending = ref(false)
const refreshing = ref(false)
const checking = ref(false)
const resolving = ref(false)
const showResolveModal = ref(false)
const selectedAlert = ref<WeatherAlertRes | null>(null)

const { data: alertsData, refresh: refreshAlerts } = await useAsyncData(
  'admin-alerts',
  async () => {
    const response = await $fetch<AlertHistoryRes>(
      '/api/admin/alerts/history'
    )
    return response
  },
  {
    server: false,
    lazy: true,
    default: () => ({ all: [], bySchedule: {} })
  }
)

// Filters
const filters = ref({
  status: 'ALL',
  severity: 'ALL',
  type: 'ALL'
})

const statusFilterOptions = [
  { value: 'ALL', label: 'Todos' },
  { value: 'PENDING', label: 'Pendiente' },
  { value: 'KEPT', label: 'Mantenido' },
  { value: 'CANCELLED', label: 'Cancelado' },
  { value: 'RESCHEDULED', label: 'Reagendado' }
]

const severityFilterOptions = [
  { value: 'ALL', label: 'Todas' },
  { value: 'LOW', label: 'Baja' },
  { value: 'MEDIUM', label: 'Media' },
  { value: 'HIGH', label: 'Alta' },
  { value: 'CRITICAL', label: 'Crítica' }
]

const typeFilterOptions = [
  { value: 'ALL', label: 'Todos' },
  { value: 'WIND', label: 'Viento' },
  { value: 'CLOUDS', label: 'Nubes' },
  { value: 'MOON', label: 'Luna' },
  { value: 'RAIN', label: 'Lluvia' }
]

// Resolve form
const resolveForm = ref({
  resolution: '',
  notes: ''
})

// Computed
const filteredAlerts = computed(() => {
  if (!alertsData.value || !alertsData.value.all) return []

  let filtered = [...alertsData.value.all]

  if (filters.value.status !== 'ALL') {
    filtered = filtered.filter(
      (alert: WeatherAlertRes) => alert.status === filters.value.status
    )
  }

  if (filters.value.severity !== 'ALL') {
    filtered = filtered.filter(
      (alert: WeatherAlertRes) => alert.severity === filters.value.severity
    )
  }

  if (filters.value.type !== 'ALL') {
    filtered = filtered.filter(
      (alert: WeatherAlertRes) => alert.alertType === filters.value.type
    )
  }

  // Sort by createdAt descending (newest first)
  return filtered.sort(
    (a: WeatherAlertRes, b: WeatherAlertRes) =>
      new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime()
  )
})

const stats = computed(() => {
  if (!alertsData.value || !alertsData.value.all)
    return { pending: 0, cancelled: 0, kept: 0, rescheduled: 0 }

  return {
    pending: alertsData.value.all.filter((a: WeatherAlertRes) => a.status === 'PENDING').length,
    cancelled: alertsData.value.all.filter((a: WeatherAlertRes) => a.status === 'CANCELLED')
      .length,
    kept: alertsData.value.all.filter((a: WeatherAlertRes) => a.status === 'KEPT').length,
    rescheduled: alertsData.value.all.filter((a: WeatherAlertRes) => a.status === 'RESCHEDULED')
      .length
  }
})

// Methods
const checkAlertsManually = async () => {
  checking.value = true
  try {
    const response = await $fetch<{ pendingAlerts: number }>(
      '/api/admin/alerts/check',
      {
        method: 'post'
      }
    )

    toast.add({
      title: 'Verificación completada',
      description: `Se encontraron ${response.pendingAlerts} alertas pendientes`,
      color: 'success'
    })

    await refreshAlerts()
  } catch (error) {
    logger.error('Error checking alerts:', error)
    toast.add({
      title: 'Error',
      description: 'No se pudo verificar las alertas',
      color: 'error'
    })
  } finally {
    checking.value = false
  }
}

const openResolveModal = (alert: WeatherAlertRes, resolution: string) => {
  selectedAlert.value = alert
  resolveForm.value = {
    resolution,
    notes: ''
  }
  showResolveModal.value = true
}

const closeResolveModal = () => {
  showResolveModal.value = false
  selectedAlert.value = null
  resolveForm.value = {
    resolution: '',
    notes: ''
  }
}

const resolveAlert = async () => {
  if (!selectedAlert.value) return

  resolving.value = true
  try {
    await $fetch(
      `/api/admin/alerts/${selectedAlert.value.id}/resolve`,
      {
        method: 'POST',
        body: {
          resolution: resolveForm.value.resolution
        }
      }
    )

    toast.add({
      title: 'Alerta resuelta',
      description: 'La alerta se ha resuelto correctamente',
      color: 'success'
    })

    await refreshAlerts()
    closeResolveModal()
  } catch (error: unknown) {
    logger.error('Error resolving alert:', error)
    const apiError = error as { data?: { error?: string } }
    toast.add({
      title: 'Error',
      description: apiError.data?.error || 'No se pudo resolver la alerta',
      color: 'error'
    })
  } finally {
    resolving.value = false
  }
}

// Helper functions
type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

function getSeverityColor(severity: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'error',
    CRITICAL: 'error'
  }
  return colors[severity] || 'neutral'
}

function getTypeColor(type: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    WIND: 'error',
    CLOUDS: 'warning',
    MOON: 'tertiary',
    RAIN: 'info'
  }
  return colors[type] || 'neutral'
}

function getTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    WIND: 'Viento',
    CLOUDS: 'Nubes',
    MOON: 'Luna',
    RAIN: 'Lluvia'
  }
  return labels[type] || type
}

// getStatusColor imported from ~/utils/adminOptions

function getResolutionColor(resolution: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    KEPT: 'success',
    CANCELLED: 'error',
    RESCHEDULED: 'warning'
  }
  return colors[resolution] || 'neutral'
}

function getResolutionLabel(resolution: string): string {
  const labels: Record<string, string> = {
    KEPT: 'Mantener Tour',
    CANCELLED: 'Cancelar Tour',
    RESCHEDULED: 'Reagendar Tour'
  }
  return labels[resolution] || resolution
}
</script>
