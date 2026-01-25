<script setup lang="ts">
import { CalendarDate } from '@internationalized/date'
import logger from '~/utils/logger'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Reportes - Admin - Northern Chile'
})

const toast = useToast()
const { formatPrice } = useCurrency()
const { formatDate } = useDateTime()
const { locale: _locale } = useI18n()
const _authStore = useAuthStore()
const { fetchReportsOverview, fetchBookingsByDayReport, fetchTopToursReport } = useAdminData()

// Date range filter using UInputDate
const today = new Date()
const thirtyDaysAgo = new Date()
thirtyDaysAgo.setDate(today.getDate() - 30)

const inputDateRef = useTemplateRef('inputDateRef')

const dateRange = shallowRef({
  start: new CalendarDate(
    thirtyDaysAgo.getFullYear(),
    thirtyDaysAgo.getMonth() + 1,
    thirtyDaysAgo.getDate()
  ),
  end: new CalendarDate(
    today.getFullYear(),
    today.getMonth() + 1,
    today.getDate()
  )
})

// Computed values for API calls
const startDate = computed(() => {
  const { start } = dateRange.value
  return `${start.year}-${String(start.month).padStart(2, '0')}-${String(start.day).padStart(2, '0')}`
})

const endDate = computed(() => {
  const { end } = dateRange.value
  return `${end.year}-${String(end.month).padStart(2, '0')}-${String(end.day).padStart(2, '0')}`
})

// Force refresh counter
const refreshKey = ref(0)

// Fetch overview data
const {
  data: overview,
  pending: loadingOverview,
  refresh: _refreshOverview,
  error: _overviewError
} = useAsyncData(
  () => `reports-overview-${refreshKey.value}`,
  async () => {
    try {
      const response = await fetchReportsOverview(startDate.value, endDate.value)
      return response
    } catch (err: unknown) {
      logger.error('Error fetching overview:', err)
      const errorData = err && typeof err === 'object' && 'data' in err
        ? (err as { data?: { message?: string } }).data
        : undefined
      toast.add({
        color: 'error',
        title: 'Error al cargar reportes',
        description: errorData?.message || 'No se pudieron cargar los datos del reporte'
      })
      return null
    }
  },
  {
    server: false,
    lazy: true
  }
)

// Fetch bookings by day
const {
  data: bookingsByDay,
  pending: loadingBookingsByDay,
  refresh: _refreshBookingsByDay
} = useAsyncData(
  () => `reports-bookings-by-day-${refreshKey.value}`,
  async () => {
    try {
      const response = await fetchBookingsByDayReport(startDate.value, endDate.value)
      return response
    } catch (err) {
      logger.error('Error fetching bookings by day:', err)
      return []
    }
  },
  {
    server: false,
    lazy: true
  }
)

// Fetch top tours
const {
  data: topTours,
  pending: loadingTopTours,
  refresh: _refreshTopTours
} = useAsyncData(
  () => `reports-top-tours-${refreshKey.value}`,
  async () => {
    try {
      const response = await fetchTopToursReport(startDate.value, endDate.value, 10)
      return response
    } catch (err) {
      logger.error('Error fetching top tours:', err)
      return []
    }
  },
  {
    server: false,
    lazy: true
  }
)

// Función para refrescar todos los datos
function handleRefresh() {
  refreshKey.value++
}
</script>

<template>
  <div class="min-h-screen p-6 bg-default">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-default">
        Reportes y Análisis
      </h1>
      <p class="mt-1 text-sm text-muted">
        Visualiza el rendimiento del negocio y métricas clave
      </p>
    </div>

    <!-- Date Range Filter -->
    <div class="bg-elevated rounded-lg p-4 mb-6 border border-default">
      <div class="flex items-end gap-4">
        <div class="flex-1">
          <label class="block text-sm font-medium text-default mb-2">
            Rango de Fechas
          </label>
          <UInputDate
            ref="inputDateRef"
            v-model="dateRange"
            range
            size="xl"
          >
            <template #trailing>
              <UPopover :reference="inputDateRef?.inputsRef[0]?.$el">
                <UButton
                  color="neutral"
                  variant="link"
                  size="sm"
                  icon="i-lucide-calendar"
                  aria-label="Seleccionar rango de fechas"
                  class="px-0"
                />

                <template #content>
                  <UCalendar
                    v-model="dateRange"
                    class="p-2"
                    :number-of-months="2"
                    range
                  />
                </template>
              </UPopover>
            </template>
          </UInputDate>
        </div>
        <UButton
          color="primary"
          icon="i-lucide-refresh-cw"
          size="xl"
          @click="handleRefresh"
        >
          Actualizar
        </UButton>
      </div>
    </div>

    <!-- Loading state -->
    <div
      v-if="loadingOverview"
      class="flex justify-center items-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <!-- Error state -->
    <div
      v-else-if="!overview"
      class="bg-elevated rounded-lg p-12 border border-default text-center"
    >
      <UIcon
        name="i-lucide-alert-circle"
        class="w-16 h-16 mx-auto mb-4 text-error"
      />
      <h3 class="text-lg font-semibold text-default mb-2">
        Error al cargar los reportes
      </h3>
      <p class="text-muted mb-4">
        No se pudieron cargar los datos. Verifica tu conexión e intenta nuevamente.
      </p>
      <UButton
        color="primary"
        @click="() => _refreshOverview()"
      >
        Reintentar
      </UButton>
    </div>

    <!-- Overview Stats -->
    <div v-else>
      <!-- Main KPIs -->
      <div class="grid grid-cols-1 gap-4 mb-6 md:grid-cols-4">
        <AdminDashboardStat
          label="Total Reservas"
          :value="overview?.totalBookings ?? 0"
          icon="i-lucide-book-marked"
          icon-color="bg-primary/10"
          icon-text-color="text-primary"
        />
        <AdminDashboardStat
          label="Reservas Confirmadas"
          :value="overview?.confirmedBookings ?? 0"
          icon="i-lucide-check-circle"
          icon-color="bg-success/10"
          icon-text-color="text-success"
        />
        <AdminDashboardStat
          label="Ingresos Totales"
          :value="formatPrice(overview?.totalRevenue ?? 0)"
          icon="i-lucide-dollar-sign"
          icon-color="bg-tertiary/10"
          icon-text-color="text-tertiary"
        />
        <AdminDashboardStat
          label="Total Participantes"
          :value="overview?.totalParticipants ?? 0"
          icon="i-lucide-users"
          icon-color="bg-info/10"
          icon-text-color="text-info"
        />
      </div>

      <!-- Secondary Metrics -->
      <div class="grid grid-cols-1 gap-4 mb-6 md:grid-cols-3">
        <AdminDashboardStat
          label="Valor Promedio Reserva"
          :value="formatPrice(overview?.averageBookingValue ?? 0)"
          icon="i-lucide-trending-up"
          icon-color="bg-secondary/10"
          icon-text-color="text-secondary"
        />
        <AdminDashboardStat
          label="Tasa de Conversión"
          :value="`${(overview?.conversionRate ?? 0).toFixed(1)}%`"
          icon="i-lucide-percent"
          icon-color="bg-warning/10"
          icon-text-color="text-warning"
        />
        <AdminDashboardStat
          label="Cancelaciones"
          :value="overview?.cancelledBookings ?? 0"
          icon="i-lucide-x-circle"
          icon-color="bg-error/10"
          icon-text-color="text-error"
        />
      </div>

      <!-- System Overview -->
      <div class="bg-elevated rounded-lg p-6 mb-6 border border-default">
        <h3 class="mb-4 text-lg font-semibold text-default">
          Resumen del Sistema
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="text-center p-4 bg-primary/5 rounded-lg border border-primary/20">
            <div class="mb-1 text-3xl font-bold text-primary">
              {{ overview?.totalUsers ?? 0 }}
            </div>
            <div class="text-sm text-muted">
              Usuarios Registrados
            </div>
          </div>
          <div class="text-center p-4 bg-success/5 rounded-lg border border-success/20">
            <div class="text-3xl font-bold text-success mb-1">
              {{ overview?.totalTours ?? 0 }}
            </div>
            <div class="text-sm text-muted">
              Tours Activos
            </div>
          </div>
          <div class="text-center p-4 bg-info/5 rounded-lg border border-info/20">
            <div class="text-3xl font-bold text-info mb-1">
              {{ overview?.totalSchedules ?? 0 }}
            </div>
            <div class="text-sm text-muted">
              Schedules Programados
            </div>
          </div>
        </div>
      </div>

      <!-- Bookings by Day -->
      <div class="bg-elevated rounded-lg p-6 mb-6 border border-default">
        <h3 class="mb-4 text-lg font-semibold text-default">
          Reservas por Día
        </h3>
        <div
          v-if="loadingBookingsByDay"
          class="flex justify-center py-8"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="w-6 h-6 animate-spin text-primary"
          />
        </div>
        <div
          v-else-if="!bookingsByDay || bookingsByDay.length === 0"
          class="text-center py-12"
        >
          <UIcon
            name="i-lucide-calendar-x"
            class="w-12 h-12 mx-auto mb-3 text-muted"
          />
          <p class="text-muted text-lg font-medium">
            No hay datos de reservas en este período
          </p>
          <p class="text-muted text-sm mt-2">
            Selecciona un rango de fechas diferente o crea algunas reservas
          </p>
        </div>
        <div
          v-else
          class="overflow-x-auto"
        >
          <table class="min-w-full divide-y divide-default">
            <thead>
              <tr>
                <th class="px-4 py-2 text-left text-xs font-medium text-muted uppercase">
                  Fecha
                </th>
                <th class="px-4 py-2 text-right text-xs font-medium text-muted uppercase">
                  Reservas
                </th>
                <th class="px-4 py-2 text-right text-xs font-medium text-muted uppercase">
                  Ingresos
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-default">
              <tr
                v-for="day in bookingsByDay"
                :key="day.date"
              >
                <td class="px-4 py-2 text-sm text-default">
                  {{ formatDate(day.date) }}
                </td>
                <td class="px-4 py-2 text-sm text-right text-default">
                  {{ day.count }}
                </td>
                <td class="px-4 py-2 text-sm text-right text-default">
                  {{ formatPrice(day.revenue) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Top Tours -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
        <h3 class="mb-4 text-lg font-semibold text-default">
          Tours Más Populares
        </h3>
        <div
          v-if="loadingTopTours"
          class="flex justify-center py-8"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="w-6 h-6 animate-spin text-primary"
          />
        </div>
        <div
          v-else-if="!topTours || topTours.length === 0"
          class="text-center py-12"
        >
          <UIcon
            name="i-lucide-map-pin-off"
            class="w-12 h-12 mx-auto mb-3 text-muted"
          />
          <p class="text-muted text-lg font-medium">
            No hay datos de tours en este período
          </p>
          <p class="text-muted text-sm mt-2">
            Las reservas confirmadas aparecerán aquí ordenadas por popularidad
          </p>
        </div>
        <div
          v-else
          class="overflow-x-auto"
        >
          <table class="min-w-full divide-y divide-default">
            <thead>
              <tr>
                <th class="px-4 py-2 text-left text-xs font-medium text-muted uppercase">
                  Tour
                </th>
                <th class="px-4 py-2 text-right text-xs font-medium text-muted uppercase">
                  Reservas
                </th>
                <th class="px-4 py-2 text-right text-xs font-medium text-muted uppercase">
                  Participantes
                </th>
                <th class="px-4 py-2 text-right text-xs font-medium text-muted uppercase">
                  Ingresos
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-default">
              <tr
                v-for="(tour, index) in topTours"
                :key="index"
              >
                <td class="px-4 py-2 text-sm text-default">
                  <div class="flex items-center gap-2">
                    <UBadge
                      :label="`#${index + 1}`"
                      :color="index === 0 ? 'primary' : 'neutral'"
                      variant="soft"
                      size="sm"
                    />
                    {{ tour.tourName }}
                  </div>
                </td>
                <td class="px-4 py-2 text-sm text-right text-default">
                  {{ tour.bookingsCount }}
                </td>
                <td class="px-4 py-2 text-sm text-right text-default">
                  {{ tour.participants }}
                </td>
                <td class="px-4 py-2 text-sm text-right text-default font-medium">
                  {{ formatPrice(tour.revenue) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
