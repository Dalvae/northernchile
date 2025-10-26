<script setup lang="ts">
import { format } from 'date-fns';
import type { BookingRes } from '~/lib/api-client';

definePageMeta({
  layout: 'admin',
});

const { fetchAdminBookings, fetchAdminTours } = useAdminData();

const { data: bookingsData, pending: pendingBookings } = await fetchAdminBookings();
const { data: tours, pending: pendingTours } = await fetchAdminTours();

// Asumimos que la respuesta de bookings es una lista
const latestBookings = computed(() => bookingsData.value?.slice(0, 5) || []);

const stats = computed(() => [
  { label: 'Ingresos Totales (placeholder)', value: '$12,345', icon: 'i-lucide-dollar-sign' },
  { label: 'Total Reservas', value: bookingsData.value?.length || 0, icon: 'i-lucide-book-marked' },
  { label: 'Total Tours', value: tours.value?.length || 0, icon: 'i-lucide-map' },
  { label: 'Nuevos Clientes (placeholder)', value: '12', icon: 'i-lucide-users' },
]);

const bookingColumns = [
  { id: 'id', key: 'id', label: 'ID' },
  { id: 'userFullName', key: 'userFullName', label: 'Cliente' },
  { id: 'tourName', key: 'tourName', label: 'Tour' },
  { id: 'status', key: 'status', label: 'Estado' },
  { id: 'totalAmount', key: 'totalAmount', label: 'Monto' },
  { id: 'createdAt', key: 'createdAt', label: 'Fecha Creación' },
];

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(amount);
}
</script>

<template>
  <div>
    <UPageHeader
      title="Dashboard"
      description="Vista general del sistema de administración."
    />

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 my-8">
      <template v-for="(stat, index) in stats" :key="index">
        <AdminDashboardStat :label="stat.label" :value="stat.value" :icon="stat.icon" />
      </template>
    </div>

    <UCard>
      <template #header>
        <h2 class="font-semibold text-lg">Últimas Reservas</h2>
      </template>
      <UTable :rows="latestBookings" :columns="bookingColumns" :loading="pendingBookings">
        <template #status-data="{ row }">
          <UBadge :color="row.status === 'CONFIRMED' ? 'green' : 'orange'" variant="subtle">
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
  </div>
</template>