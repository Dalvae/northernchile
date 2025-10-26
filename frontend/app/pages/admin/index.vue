<script setup lang="ts">
import { format } from 'date-fns';
import type { BookingRes } from '~/lib/api-client';

definePageMeta({
  layout: 'admin',
});

const { fetchAdminBookings, fetchAdminTours } = useAdminData();

// Hacemos que la respuesta de la API sea reactiva
const { data: bookingsResponse, pending: pendingBookings } = await useAsyncData('latestBookings', () => fetchAdminBookings({ limit: 5, sortBy: 'createdAt', order: 'desc' }));
const { data: toursResponse, pending: pendingTours } = await useAsyncData('adminTours', () => fetchAdminTours());

// Extraemos los datos de la respuesta
const latestBookings = computed(() => bookingsResponse.value?.data || []);
const totalBookings = computed(() => bookingsResponse.value?.totalElements || 0);
const tours = computed(() => toursResponse.value || []);

const stats = computed(() => [
  { label: 'Ingresos Totales (placeholder)', value: '$12,345', icon: 'i-lucide-dollar-sign' },
  { label: 'Total Reservas', value: totalBookings.value, icon: 'i-lucide-book-marked' },
  { label: 'Total Tours', value: tours.value.length, icon: 'i-lucide-map' },
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

    <!-- Estadísticas -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 my-8">
      <template v-for="(stat, index) in stats" :key="index">
        <!-- Corregimos el nombre del componente -->
        <AdminDashboardStat :label="stat.label" :value="stat.value" :icon="stat.icon" />
      </template>
    </div>

    <!-- Últimas Reservas -->
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