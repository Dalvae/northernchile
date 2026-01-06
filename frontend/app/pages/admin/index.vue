<script setup lang="ts">
import { format } from 'date-fns'
import type { BookingRes } from 'api-client'
import { getStatusColor } from '~/utils/adminOptions'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Dashboard - Admin - Northern Chile'
})

const { fetchAdminBookings, fetchAdminAlertsCount } = useAdminData()
const { formatPrice: formatCurrency } = useCurrency()
const { formatDate, formatLocalTime } = useDateTime()

const adminStore = useAdminStore()

const { data: bookingsData, pending: pendingBookings } = await useAsyncData(
  'admin-bookings-dashboard',
  () => fetchAdminBookings(),
  { server: false, lazy: true, default: () => [] }
)
const { pending: pendingTours } = useAdminToursData()

const tours = computed(() => adminStore.tours)

// Fetch alerts count (reuse admin data + proxy)
const { data: alertsCount } = await useAsyncData(
  'dashboard-alerts-count',
  async () => {
    try {
      const response = await fetchAdminAlertsCount()
      return response.pending || 0
    } catch {
      return 0
    }
  },
  {
    server: false,
    lazy: true
  }
)

const latestBookings = computed<BookingRes[]>(() => {
  const items = bookingsData.value || []
  if (!Array.isArray(items) || items.length === 0) return []
  return [...items]
    .filter(b => b.status === 'CONFIRMED')
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5)
})

// Stats calculados con datos reales
const stats = computed(() => {
  const allTours = Array.isArray(tours.value) ? tours.value : []
  const allBookings = Array.isArray(bookingsData.value) ? bookingsData.value : []
  const totalBookings = allBookings.length
  const activeTours = allTours.filter(t => t.status === 'PUBLISHED').length

  // Calcular ingresos del mes actual
  const now = new Date()
  const currentMonth = now.getMonth()
  const currentYear = now.getFullYear()

  const monthlyRevenue = allBookings
    .filter((b) => {
      if (!b.createdAt) return false
      const bookingDate = new Date(b.createdAt)
      return bookingDate.getMonth() === currentMonth
        && bookingDate.getFullYear() === currentYear
        && (b.status === 'CONFIRMED' || b.status === 'COMPLETED')
    })
    .reduce((sum, b) => sum + b.totalAmount, 0)

  return [
    {
      label: 'Total Reservas',
      value: totalBookings,
      icon: 'i-lucide-book-marked',
      iconColor: 'bg-primary/10',
      iconTextColor: 'text-primary',
      loading: pendingBookings.value
    },
    {
      label: 'Ingresos del Mes',
      value: formatCurrency(monthlyRevenue),
      icon: 'i-lucide-dollar-sign',
      iconColor: 'bg-success/10',
      iconTextColor: 'text-success',
      loading: pendingBookings.value
    },
    {
      label: 'Tours Activos',
      value: activeTours,
      icon: 'i-lucide-map',
      iconColor: 'bg-info/10',
      iconTextColor: 'text-info',
      loading: pendingTours.value
    },
    {
      label: 'Alertas Pendientes',
      value: alertsCount.value || 0,
      icon: 'i-lucide-alert-triangle',
      iconColor: 'bg-warning/10',
      iconTextColor: 'text-warning',
      loading: false
    }
  ]
})

const bookingColumns = [
  { accessorKey: 'tourName', header: 'Tour' },
  {
    accessorKey: 'tourDate',
    header: 'Fecha/Hora',
    cell: ({ row }: { row: import('@tanstack/vue-table').Row<BookingRes> }) => {
      const date = row.original.tourDate || row.getValue('tourDate')
      const time = row.original.tourStartTime
      if (!date) return ''
      const formattedDate = formatDate(date as string)
      const timeStr = formatLocalTime(time)
      return `${formattedDate}${timeStr ? ` - ${timeStr}` : ''}`
    }
  },
  { accessorKey: 'userFullName', header: 'Cliente' },
  { accessorKey: 'status', header: 'Estado' },
  { accessorKey: 'totalAmount', header: 'Monto' },
  { id: 'actions', header: '' }
]

// Status color imported from ~/utils/adminOptions
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-default">
        Dashboard
      </h1>
      <p class="mt-1 text-sm text-muted">
        Vista general del sistema de administración
      </p>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <AdminDashboardStat
        v-for="(stat, index) in stats"
        :key="index"
        :label="stat.label"
        :value="stat.value"
        :icon="stat.icon"
        :icon-color="stat.iconColor"
        :icon-text-color="stat.iconTextColor"
        :loading="stat.loading"
      />
    </div>

    <!-- Latest Bookings Card -->
    <UCard
      class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
    >
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="font-semibold text-lg text-default">
            Últimas Reservas
          </h2>
          <UButton
            to="/admin/bookings"
            variant="link"
            trailing-icon="i-lucide-arrow-right"
          >
            Ver todas
          </UButton>
        </div>
      </template>

      <UTable
        :data="latestBookings"
        :columns="bookingColumns"
        :loading="pendingBookings"
        :ui="{
          td: 'p-4 text-sm text-default whitespace-nowrap [&:has([role=checkbox])]:pe-0'
        }"
      >
        <template #tourName-data="{ row }">
          <div class="flex flex-col">
            <span class="font-medium">
              {{ row.getValue('tourName') }}
            </span>
            <span
              v-if="row.original.tourDate"
              class="text-xs text-default"
            >
              {{ format(new Date(row.original.tourDate), 'EEEE, dd MMMM yyyy') }}
              <span v-if="row.original.tourStartTime">
                - {{ formatLocalTime(row.original.tourStartTime) }}
              </span>
            </span>
          </div>
        </template>
        <template #status-data="{ row }">
          <UBadge
            :color="getStatusColor(row.original.status || 'PENDING')"
            variant="subtle"
          >
            {{ row.original.status }}
          </UBadge>
        </template>
        <template #totalAmount-data="{ row }">
          <span>{{ formatCurrency(row.original.totalAmount) }}</span>
        </template>
        <template #actions-data="{ row }">
          <AdminBookingsBookingDetailsModal :booking="row.original" />
        </template>
      </UTable>
    </UCard>
  </div>
</template>
