<script setup lang="ts">
import { format } from 'date-fns'
import type { BookingRes } from '~/lib/api-client'

definePageMeta({
  layout: 'admin'
});

const { fetchAdminBookings, fetchAdminTours } = useAdminData();

const { data: bookingsData, pending: pendingBookings } = await fetchAdminBookings({ limit: 5, sortBy: 'createdAt', order: 'desc' });
const { data: tours, pending: pendingTours } = await fetchAdminTours();

const latestBookings = computed(() => bookingsData.value?.data || [])

const stats = computed(() => [
  { label: 'Ingresos Totales', value: '$12,345', icon: 'i-lucide-dollar-sign' },
  { label: 'Total Reservas', value: bookingsData.value?.totalElements || 0, icon: 'i-lucide-book-marked' },
  { label: 'Total Tours', value: tours.value?.length || 0, icon: 'i-lucide-map' },
  { label: 'Nuevos Clientes (Mes)', value: '12', icon: 'i-lucide-users' },
]);

const bookingColumns = [
  { key: 'id', label: 'ID' },
  { key: 'userFullName', label: 'Cliente' },
  { key: 'tourName', label: 'Tour' },
  { key: 'status', label: 'Estado' },
  { key: 'totalAmount', label: 'Monto' },
  { key: 'createdAt', label: 'Fecha Creación' },
]

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(amount);
}
</script>

<template>
  <UDashboardPanel grow>
    <UDashboardNavbar title="Dashboard" />
    <UDashboardPanelContent>
      <!-- Estadísticas -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <template v-for="(stat, index) in stats" :key="index">
          <DashboardStat :label="stat.label" :value="stat.value" :icon="stat.icon" />
        </template>
      </div>

      <!-- Últimas Reservas -->
      <UCard>
        <template #header>
          <h2 class="font-semibold text-lg">Últimas Reservas</h2>
        </template>
        <UTable :rows="latestBookings" :columns="bookingColumns" :loading="pendingBookings">
           <template #status-data="{ row }">
            <UBadge
              :color="row.status === 'CONFIRMED' ? 'green' : 'orange'"
              variant="subtle"
            >
              {{ row.status }}
            </UBadge>
          </template>
          <template #totalAmount-data="{ row }">
            <span>{{ formatCurrency(row.totalAmount) }}</span>
          </template>
           <template #createdAt-data="{ row }">
            <span>{{ format(new Date(row.createdAt), 'dd MMM yyyy, HH:mm') }}</span>
          </template>
        </UTable>
      </UCard>

    </UDashboardPanelContent>
  </UDashboardPanel>
</template>