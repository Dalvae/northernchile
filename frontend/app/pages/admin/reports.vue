<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

const config = useRuntimeConfig()
const toast = useToast()
const { formatPrice } = useCurrency()

// Date range filter
const today = new Date()
const thirtyDaysAgo = new Date()
thirtyDaysAgo.setDate(today.getDate() - 30)

const dateRange = ref({
  start: thirtyDaysAgo.toISOString().split('T')[0],
  end: today.toISOString().split('T')[0]
})

// Fetch overview data
const { data: overview, pending: overviewPending, refresh: refreshOverview } = await useAsyncData(
  'reports-overview',
  async () => {
    try {
      const response = await $fetch<{
        periodStart: string
        periodEnd: string
        totalBookings: number
        confirmedBookings: number
        cancelledBookings: number
        totalRevenue: number
        totalParticipants: number
        averageBookingValue: number
        conversionRate: number
        totalUsers: number
        totalTours: number
        totalSchedules: number
      }>(`${config.public.apiBaseUrl}/admin/reports/overview`, {
        params: {
          startDate: dateRange.value.start,
          endDate: dateRange.value.end
        },
        credentials: 'include'
      })
      return response
    } catch (err) {
      console.error('Error fetching overview:', err)
      toast.add({
        color: 'error',
        title: 'Error',
        description: 'No se pudieron cargar los datos del reporte'
      })
      throw err
    }
  },
  {
    server: false,
    lazy: true,
    watch: [dateRange]
  }
)

// Fetch bookings by day
const { data: bookingsByDay, pending: bookingsByDayPending } = await useAsyncData(
  'reports-bookings-by-day',
  async () => {
    try {
      const response = await $fetch<Array<{
        date: string
        count: number
        revenue: number
      }>>(`${config.public.apiBaseUrl}/admin/reports/bookings-by-day`, {
        params: {
          startDate: dateRange.value.start,
          endDate: dateRange.value.end
        },
        credentials: 'include'
      })
      return response
    } catch (err) {
      console.error('Error fetching bookings by day:', err)
      return []
    }
  },
  {
    server: false,
    lazy: true,
    watch: [dateRange]
  }
)

// Fetch top tours
const { data: topTours, pending: topToursPending } = await useAsyncData(
  'reports-top-tours',
  async () => {
    try {
      const response = await $fetch<Array<{
        tourName: string
        bookingsCount: number
        revenue: number
        participants: number
      }>>(`${config.public.apiBaseUrl}/admin/reports/top-tours`, {
        params: {
          startDate: dateRange.value.start,
          endDate: dateRange.value.end,
          limit: 10
        },
        credentials: 'include'
      })
      return response
    } catch (err) {
      console.error('Error fetching top tours:', err)
      return []
    }
  },
  {
    server: false,
    lazy: true,
    watch: [dateRange]
  }
)

// Format date
const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('es-CL', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
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
      <div class="flex items-center gap-4">
        <div class="flex-1">
          <label class="block text-sm font-medium text-default mb-2">
            Rango de Fechas
          </label>
          <div class="grid grid-cols-2 gap-4">
            <UInput
              v-model="dateRange.start"
              type="date"
              label="Desde"
              size="lg"
            />
            <UInput
              v-model="dateRange.end"
              type="date"
              label="Hasta"
              size="lg"
            />
          </div>
        </div>
        <div class="flex items-end">
          <UButton
            color="primary"
            icon="i-lucide-refresh-cw"
            :loading="overviewPending"
            @click="() => refreshOverview()"
          >
            Actualizar
          </UButton>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div
      v-if="overviewPending"
      class="flex justify-center items-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <!-- Overview Stats -->
    <div v-else-if="overview">
      <!-- Main KPIs -->
       <div class="grid grid-cols-1 gap-4 mb-6 md:grid-cols-4">
        <AdminDashboardStat
          label="Total Reservas"
          :value="overview.totalBookings"
          icon="i-lucide-book-marked"
          icon-color="bg-primary/10"
          icon-text-color="text-primary"
        />
        <AdminDashboardStat
          label="Reservas Confirmadas"
          :value="overview.confirmedBookings"
          icon="i-lucide-check-circle"
          icon-color="bg-success/10"
          icon-text-color="text-success"
        />
        <AdminDashboardStat
          label="Ingresos Totales"
          :value="formatPrice(overview.totalRevenue)"
          icon="i-lucide-dollar-sign"
          icon-color="bg-tertiary/10"
          icon-text-color="text-tertiary"
        />
        <AdminDashboardStat
          label="Total Participantes"
          :value="overview.totalParticipants"
          icon="i-lucide-users"
          icon-color="bg-info/10"
          icon-text-color="text-info"
        />
      </div>

      <!-- Secondary Metrics -->
       <div class="grid grid-cols-1 gap-4 mb-6 md:grid-cols-3">
        <AdminDashboardStat
          label="Valor Promedio Reserva"
          :value="formatPrice(overview.averageBookingValue)"
          icon="i-lucide-trending-up"
          icon-color="bg-secondary/10"
          icon-text-color="text-secondary"
        />
        <AdminDashboardStat
          label="Tasa de Conversión"
          :value="`${(overview.conversionRate || 0).toFixed(1)}%`"
          icon="i-lucide-percent"
          icon-color="bg-warning/10"
          icon-text-color="text-warning"
        />
        <AdminDashboardStat
          label="Cancelaciones"
          :value="overview.cancelledBookings"
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
        <div class="grid grid-cols-3 gap-4">
          <div class="text-center">
            <div class="mb-1 text-3xl font-bold text-primary">
              {{ overview.totalUsers }}
            </div>
             <div class="text-sm text-muted">
              Usuarios Registrados
            </div>
          </div>
          <div class="text-center">
            <div class="text-3xl font-bold text-success mb-1">
              {{ overview.totalTours }}
            </div>
             <div class="text-sm text-muted">
              Tours Activos
            </div>
          </div>
          <div class="text-center">
            <div class="text-3xl font-bold text-info mb-1">
              {{ overview.totalSchedules }}
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
          v-if="bookingsByDayPending"
          class="flex justify-center py-8"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="w-6 h-6 animate-spin text-primary"
          />
        </div>
        <div
          v-else-if="bookingsByDay && bookingsByDay.length > 0"
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
        <div
          v-else
          class="text-center py-8"
        >
          <p class="text-muted">
            No hay datos de reservas en este período
          </p>
        </div>
      </div>

      <!-- Top Tours -->
      <div class="bg-elevated rounded-lg p-6 border border-default">
         <h3 class="mb-4 text-lg font-semibold text-default">
          Tours Más Populares
        </h3>
        <div
          v-if="topToursPending"
          class="flex justify-center py-8"
        >
          <UIcon
            name="i-lucide-loader-2"
            class="w-6 h-6 animate-spin text-primary"
          />
        </div>
        <div
          v-else-if="topTours && topTours.length > 0"
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
        <div
          v-else
          class="text-center py-8"
        >
          <p class="text-muted">
            No hay datos de tours en este período
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
