<script setup lang="ts">
import type { TourRes } from "~/lib/api-client";

definePageMeta({
  layout: "admin",
});

// --- OBTENCIÓN DE DATOS ---
const { fetchAdminTours, deleteAdminTour } = useAdminData();
const {
  data: toursResponse,
  pending,
  refresh,
} = await useAsyncData("admin-tours", () => fetchAdminTours());
const tours = computed(() => toursResponse.value?.data || []);

// --- ESTADO ---
const selectedTour = ref<TourRes | null>(null);

// --- BÚSQUEDA Y COLUMNAS ---
const q = ref("");
const columns = [
  { id: "name", key: "name", label: "Nombre", sortable: true },
  { id: "category", key: "category", label: "Categoría", sortable: true },
  {
    id: "priceAdult",
    key: "priceAdult",
    label: "Precio Adulto",
    sortable: true,
  },
  { id: "status", key: "status", label: "Estado", sortable: true },
  { id: "actions", key: "actions", label: "Acciones", sortable: false },
];

const filteredRows = computed(() => {
  if (!q.value) return tours.value;
  return tours.value.filter(
    (tour) =>
      tour.name && tour.name.toLowerCase().includes(q.value.toLowerCase()),
  );
});

// --- FUNCIONES MODAL ---
function openCreateModal() {
  selectedTour.value = null;
}

function openEditModal(tour: TourRes) {
  selectedTour.value = tour;
}

function onModalSuccess() {
  refresh();
}

// --- ELIMINACIÓN ---
const toast = useToast();
async function handleDelete(tour: TourRes) {
  if (confirm(`¿Estás seguro de que quieres eliminar "${tour.name}"?`)) {
    try {
      if (!tour.id) return;
      await deleteAdminTour(tour.id);
      toast.add({ title: "Tour eliminado", color: "green" });
      await refresh();
    } catch (e: any) {
      toast.add({
        title: "Error al eliminar",
        description: e.message,
        color: "red",
      });
    }
  }
}

// --- DROPDOWN ACTIONS ---
const items = (row: TourRes) => [
  [
    {
      label: "Editar",
      icon: "i-lucide-pencil",
      click: () => openEditModal(row),
    },
  ],
  [
    {
      label: "Eliminar",
      icon: "i-lucide-trash-2",
      click: () => handleDelete(row),
    },
  ],
];
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- HEADER -->
    <div
      class="border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900"
    >
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
              Gestión de Tours
            </h1>
            <p class="text-gray-600 dark:text-gray-400 mt-1">
              Administra y crea nuevos tours
            </p>
          </div>

          <div class="flex items-center gap-3">
            <UInput
              v-model="q"
              icon="i-lucide-search"
              placeholder="Buscar tour por nombre..."
              class="w-80"
            />

            <!-- MODAL PARA CREAR - AHORA EL BOTÓN ESTÁ DENTRO DEL COMPONENTE -->
            <AdminToursTourModal
              :tour="selectedTour"
              @success="onModalSuccess"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- TABLA -->
    <div class="p-6">
      <div
        class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden"
      >
        <UTable
          :rows="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-map',
            label: 'No hay tours creados.',
          }"
          :ui="{
            thead: 'bg-gray-50 dark:bg-gray-700',
            th: 'px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider',
            td: 'px-4 py-3 whitespace-nowrap',
          }"
        >
          <template #category-data="{ row }">
            <UBadge color="blue" variant="subtle" class="capitalize">
              {{ row.category.toLowerCase() }}
            </UBadge>
          </template>

          <template #priceAdult-data="{ row }">
            <span class="font-medium">
              {{
                new Intl.NumberFormat("es-CL", {
                  style: "currency",
                  currency: "CLP",
                }).format(row.priceAdult)
              }}
            </span>
          </template>

          <template #status-data="{ row }">
            <UBadge
              :color="
                row.status === 'PUBLISHED'
                  ? 'green'
                  : row.status === 'DRAFT'
                    ? 'yellow'
                    : 'red'
              "
              variant="subtle"
              class="capitalize"
            >
              {{
                row.status === "PUBLISHED"
                  ? "Publicado"
                  : row.status === "DRAFT"
                    ? "Borrador"
                    : "Archivado"
              }}
            </UBadge>
          </template>

          <template #actions-data="{ row }">
            <div class="flex justify-end">
              <UDropdown :items="items(row)" :ui="{ width: 'w-48' }">
                <UButton
                  color="gray"
                  variant="ghost"
                  icon="i-lucide-more-horizontal"
                  size="sm"
                />
              </UDropdown>
            </div>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>
