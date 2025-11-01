<script setup lang="ts">
import { h } from "vue";
import type { BookingRes } from "~/lib/api-client";

definePageMeta({
  layout: "admin",
});

const { fetchAdminBookings, updateAdminBooking, deleteAdminBooking } = useAdminData();
const { formatPrice: formatCurrency } = useCurrency();

const {
  data: bookings,
  pending,
  refresh,
} = useAsyncData(
  "admin-bookings",
  () => fetchAdminBookings(),
  {
    server: false,
    lazy: true,
    default: () => []
  }
);

const isDetailsModalOpen = ref(false);
const selectedBooking = ref<BookingRes | null>(null);
const q = ref("");
const statusFilter = ref<string>("ALL");

const columns = [
  {
    id: "id",
    accessorKey: "id",
    header: "ID Reserva",
  },
  {
    id: "tourDate",
    accessorKey: "tourDate",
    header: "Fecha Tour",
  },
  {
    id: "tourName",
    accessorKey: "tourName",
    header: "Tour",
  },
  {
    id: "userFullName",
    accessorKey: "userFullName",
    header: "Cliente",
  },
  {
    id: "participantsCount",
    header: "Participantes",
  },
  {
    id: "totalAmount",
    accessorKey: "totalAmount",
    header: "Total",
  },
  {
    id: "status",
    accessorKey: "status",
    header: "Estado",
  },
  {
    id: "actions",
    header: "Acciones",
  },
];

const statusOptions = [
  { label: "Todas", value: "ALL" },
  { label: "Pendientes", value: "PENDING" },
  { label: "Confirmadas", value: "CONFIRMED" },
  { label: "Canceladas", value: "CANCELLED" },
];

const filteredRows = computed(() => {
  if (!bookings.value || bookings.value.length === 0) return [];

  let rows = bookings.value;

  // Filter by search query
  if (q.value) {
    const query = q.value.toLowerCase();
    rows = rows.filter((booking) =>
      booking.userFullName?.toLowerCase().includes(query) ||
      booking.tourName?.toLowerCase().includes(query) ||
      booking.id?.toLowerCase().includes(query)
    );
  }

  // Filter by status
  if (statusFilter.value && statusFilter.value !== "ALL") {
    rows = rows.filter((booking) => booking.status === statusFilter.value);
  }

  return rows;
});

function openDetailsModal(booking: BookingRes) {
  selectedBooking.value = booking;
  isDetailsModalOpen.value = true;
}

function closeModal() {
  isDetailsModalOpen.value = false;
  selectedBooking.value = null;
}

function onModalSuccess() {
  closeModal();
  refresh();
}

const toast = useToast();

async function handleStatusChange(booking: BookingRes, newStatus: string) {
  try {
    await updateAdminBooking(booking.id, { status: newStatus });
    toast.add({
      title: "Estado actualizado",
      color: "success",
      icon: "i-lucide-check-circle",
    });
    await refresh();
  } catch (e: any) {
    toast.add({
      title: "Error al actualizar",
      description: e.message || "Error desconocido",
      color: "error",
      icon: "i-lucide-x-circle",
    });
  }
}

async function handleCancel(booking: BookingRes) {
  const clientName = booking.userFullName || "este cliente";
  if (confirm(`¿Estás seguro de que quieres cancelar la reserva de "${clientName}"?`)) {
    try {
      await deleteAdminBooking(booking.id);
      toast.add({
        title: "Reserva cancelada",
        color: "success",
        icon: "i-lucide-check-circle",
      });
      await refresh();
    } catch (e: any) {
      toast.add({
        title: "Error al cancelar",
        description: e.message || "Error desconocido",
        color: "error",
        icon: "i-lucide-x-circle",
      });
    }
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString("es-CL", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-900">
    <div
      class="border-b border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900"
    >
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
              Gestión de Reservas
            </h1>
            <p class="text-neutral-600 dark:text-neutral-400 mt-1">
              Administra todas las reservas del sistema
            </p>
          </div>
          <div class="flex items-center gap-3">
            <UInput
              v-model="q"
              icon="i-lucide-search"
              placeholder="Buscar reserva..."
              class="w-80"
            />
            <USelectMenu
              v-model="statusFilter"
              :options="statusOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Filtrar por estado"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div
        class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm border border-neutral-200 dark:border-neutral-700 overflow-hidden"
      >
        <UTable
          :data="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-calendar-x',
            label: 'No hay reservas registradas.',
          }"
        >
          <template #id-data="{ row }">
            <span class="font-mono text-xs text-neutral-600 dark:text-neutral-400">
              {{ row.getValue("id")?.slice(0, 8) }}...
            </span>
          </template>

          <template #tourDate-data="{ row }">
            <span class="font-medium">
              {{ formatDate(row.getValue("tourDate")) }}
            </span>
          </template>

          <template #tourName-data="{ row }">
            <span class="text-sm">
              {{ row.getValue("tourName") || "Sin nombre" }}
            </span>
          </template>

          <template #userFullName-data="{ row }">
            <span class="font-medium">
              {{ row.getValue("userFullName") || "Sin nombre" }}
            </span>
          </template>

          <template #participantsCount-cell="{ row }">
            <span class="font-semibold">
              {{ row.original.participants?.length || 0 }}
            </span>
          </template>

          <template #totalAmount-data="{ row }">
            <span class="font-semibold">
              {{ formatCurrency(row.getValue("totalAmount")) }}
            </span>
          </template>

          <template #status-data="{ row }">
            <UBadge
              :color="
                row.getValue('status') === 'CONFIRMED'
                  ? 'success'
                  : row.getValue('status') === 'PENDING'
                  ? 'warning'
                  : 'error'
              "
              variant="subtle"
            >
              {{
                row.getValue("status") === "CONFIRMED"
                  ? "Confirmada"
                  : row.getValue("status") === "PENDING"
                  ? "Pendiente"
                  : "Cancelada"
              }}
            </UBadge>
          </template>

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-eye"
                color="neutral"
                variant="ghost"
                size="sm"
                aria-label="Ver detalles"
                @click="openDetailsModal(row.original)"
              />

              <UDropdownMenu
                :items="[
                  [
                    {
                      label: 'Confirmar',
                      icon: 'i-lucide-check',
                      disabled: row.original.status === 'CONFIRMED',
                      click: () => handleStatusChange(row.original, 'CONFIRMED')
                    },
                    {
                      label: 'Marcar Pendiente',
                      icon: 'i-lucide-clock',
                      disabled: row.original.status === 'PENDING',
                      click: () => handleStatusChange(row.original, 'PENDING')
                    }
                  ],
                  [
                    {
                      label: 'Cancelar',
                      icon: 'i-lucide-x-circle',
                      disabled: row.original.status === 'CANCELLED',
                      click: () => handleCancel(row.original)
                    }
                  ]
                ]"
              >
                <UButton
                  icon="i-lucide-more-vertical"
                  color="neutral"
                  variant="ghost"
                  size="sm"
                  aria-label="Más opciones"
                />
              </UDropdownMenu>
            </div>
          </template>
        </UTable>
      </div>
    </div>

    <!-- Details Modal -->
    <AdminBookingsBookingDetailsModal
      v-model:open="isDetailsModalOpen"
      :booking="selectedBooking"
      @close="closeModal"
      @success="onModalSuccess"
    />
  </div>
</template>
