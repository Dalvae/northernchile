<script setup lang="ts">
import { format } from 'date-fns'
import type { BookingRes } from '~/lib/api-client'

definePageMeta({
  layout: 'admin'
})

const { fetchAdminBookings, fetchAdminTours } = useAdminData()

const { data: bookingsData, pending: pendingBookings } = await useAsyncData(
  'bookings',
  () => fetchAdminBookings()
)
const { data: tours, pending: pendingTours } = await useAsyncData('tours', () =>
  fetchAdminTours()
)

const latestBookings = computed(() =>
  Array.isArray(bookingsData.value?.data)
    ? bookingsData.value.data.slice(0, 5)
    : []
)

// Stats con loading state y cambio porcentual simulado
const stats = computed(() => {
  const allTours = Array.isArray(tours.value?.data) ? tours.value.data : []
  const totalBookings = bookingsData.value?.data.length || 0
  const activeTours = allTours.filter((t) => t.status === 'PUBLISHED').length || 0

  return [
    {
      label: 'Total Reservas',
      value: totalBookings,
      icon: 'i-lucide-book-marked',
      change: 12.5,
      iconColor: 'bg-primary/10',
      iconTextColor: 'text-primary',
      loading: pendingBookings
    },
    {
      label: 'Ingresos del Mes',
      value: '$12,345',
      icon: 'i-lucide-dollar-sign',
      change: 8.2,
      iconColor: 'bg-success/10',
      iconTextColor: 'text-success',
      loading: pendingBookings
    },
    {
      label: 'Tours Activos',
      value: activeTours,
      icon: 'i-lucide-map',
      iconColor: 'bg-info/10',
      iconTextColor: 'text-info',
      loading: pendingTours
    },
    {
      label: 'Alertas Pendientes',
      value: '3',
      icon: 'i-lucide-alert-triangle',
      change: -25,
      iconColor: 'bg-warning/10',
      iconTextColor: 'text-warning',
      loading: false
    }
  ]
})

const bookingColumns = [
  { key: 'id', id: 'id', label: 'ID' },
  { key: 'userFullName', id: 'userFullName', label: 'Cliente' },
  { key: 'tourName', id: 'tourName', label: 'Tour' },
  { key: 'status', id: 'status', label: 'Estado' },
  { key: 'totalAmount', id: 'totalAmount', label: 'Monto' },
  { key: 'createdAt', id: 'createdAt', label: 'Fecha Creación' },
  { key: 'actions', id: 'actions', label: 'Acciones' }
]

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(amount)
}

function getStatusColor(status: string) {
  const colors: Record<string, string> = {
    'CONFIRMED': 'success',
    'PENDING': 'warning',
    'CANCELLED': 'error',
    'COMPLETED': 'info'
  }
  return colors[status] || 'neutral'
}

const actions = (row: BookingRes) => [
  [
    {
      label: 'Ver Detalles',
      icon: 'i-lucide-eye'
      // Add click handler later: click: () => console.log('View', row.id)
    }
  ]
]
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
        Dashboard
      </h1>
      <p class="mt-1 text-sm text-neutral-600 dark:text-neutral-400">
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
        :change="stat.change"
        :icon-color="stat.iconColor"
        :icon-text-color="stat.iconTextColor"
        :loading="stat.loading"
      />
    </div>

    <!-- Latest Bookings Card -->
    <UCard>
      <template #header>
        <div class="flex justify-between items-center">
          <h2 class="font-semibold text-lg text-neutral-900 dark:text-white">
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
        :rows="latestBookings"
        :columns="bookingColumns"
        :loading="pendingBookings"
      >
        <template #status-data="{ row }">
          <UBadge
            :color="getStatusColor(row.status)"
            variant="subtle"
          >
            {{ row.status }}
          </UBadge>
        </template>
        <template #totalAmount-data="{ row }">
          <span>{{ formatCurrency(row.totalAmount) }}</span>
        </template>
        <template #createdAt-data="{ row }">
          <span>{{
            format(new Date(row.createdAt), 'dd MMM yyyy, HH:mm')
          }}</span>
        </template>
        <template #actions-data="{ row }">
          <UDropdownMenu :items="actions(row)">
            <UButton
              color="neutral"
              variant="ghost"
              icon="i-lucide-more-horizontal"
            />
          </UDropdownMenu>
        </template>
      </UTable>
    </UCard>
  </div>
</template>
