<script setup lang="ts">
import { format } from "date-fns";
import type { BookingRes } from "~/lib/api-client";

definePageMeta({
  layout: "admin",
});

const { fetchAdminBookings, fetchAdminTours } = useAdminData();

const { data: bookingsData, pending: pendingBookings } = await useAsyncData(
  "bookings",
  () => fetchAdminBookings(),
);
const { data: tours, pending: pendingTours } = await useAsyncData("tours", () =>
  fetchAdminTours(),
);

const latestBookings = computed(() =>
  Array.isArray(bookingsData.value?.data)
    ? bookingsData.value.data.slice(0, 5)
    : [],
);

const stats = computed(() => {
  const allTours = Array.isArray(tours.value?.data) ? tours.value.data : []; // Aseguramos que allTours sea siempre un array
  return [
    {
      label: "Ingresos Totales (placeholder)",
      value: "$12,345",
      icon: "i-lucide-dollar-sign",
    },
    {
      label: "Total Reservas",
      value: bookingsData.value?.data.length || 0,
      icon: "i-lucide-book-marked",
    },
    {
      label: "Total Tours Activos",
      value: allTours.filter((t) => t.status === "PUBLISHED").length || 0,
      icon: "i-lucide-map",
    },
    {
      label: "Nuevos Clientes (placeholder)",
      value: "12",
      icon: "i-lucide-users",
    },
  ];
});

const bookingColumns = [
  { key: "id", id: "id", label: "ID" },
  { key: "userFullName", id: "userFullName", label: "Cliente" },
  { key: "tourName", id: "tourName", label: "Tour" },
  { key: "status", id: "status", label: "Estado" },
  { key: "totalAmount", id: "totalAmount", label: "Monto" },
  { key: "createdAt", id: "createdAt", label: "Fecha Creación" },
  { key: "actions", id: "actions", label: "Acciones" },
];

function formatCurrency(amount: number) {
  return new Intl.NumberFormat("es-CL", {
    style: "currency",
    currency: "CLP",
  }).format(amount);
}

const actions = (row: BookingRes) => [
  [
    {
      label: "Ver Detalles",
      icon: "i-lucide-eye",
      // Add click handler later: click: () => console.log('View', row.id)
    },
  ],
];
</script>
<template>
  <UPage>
    <UPageHeader
      title="Dashboard"
      description="Vista general del sistema de administración."
    />
    <UPageBody>
      <!-- Stats Grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <template v-for="(stat, index) in stats" :key="index">
          <AdminDashboardStat
            :label="stat.label"
            :value="stat.value"
            :icon="stat.icon"
          />
        </template>
      </div>

      <!-- Latest Bookings Card -->
      <UCard>
        <template #header>
          <div class="flex justify-between items-center">
            <h2 class="font-semibold text-lg">Últimas Reservas</h2>
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
            <span>{{
              format(new Date(row.createdAt), "dd MMM yyyy, HH:mm")
            }}</span>
          </template>
          <template #actions-data="{ row }">
            <UDropdownMenu :items="actions(row)">
              <UButton
                color="gray"
                variant="ghost"
                icon="i-lucide-more-horizontal"
              />
            </UDropdownMenu>
          </template>
        </UTable>
      </UCard>
    </UPageBody>
  </UPage>
</template>
