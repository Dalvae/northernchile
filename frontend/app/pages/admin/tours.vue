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

const tours = computed(() => toursResponse.value || []);

// --- ESTADO ---
const selectedTour = ref<TourRes | null>(null);

// --- BÚSQUEDA Y COLUMNAS ---
const q = ref("");

// ESTA ES LA CONFIGURACIÓN DE COLUMNAS CORRECTA Y ROBUSTA
const columns = [
  // Para columnas con slots, necesitamos un 'key' para el slot y un 'id' para la tabla.
  // Hacerlos coincidir es la práctica más segura.
  { key: "name", id: "name", label: "Nombre (ES)", sortable: true },

  { key: "category", id: "category", label: "Categoría", sortable: true },
  {
    key: "priceAdult",
    id: "priceAdult",
    label: "Precio Adulto",
    sortable: true,
  },
  { key: "status", id: "status", label: "Estado", sortable: true },

  // La columna de acciones también necesita un 'id' para ser identificada.
  { key: "actions", id: "actions", label: "Acciones", sortable: false },
];

const filteredRows = computed(() => {
  if (!q.value) {
    return tours.value;
  }
  return tours.value.filter((tour) => {
    // Aseguramos que la búsqueda funcione aunque nameTranslations no exista
    const name = tour.nameTranslations?.es || "";
    return name.toLowerCase().includes(q.value.toLowerCase());
  });
});

// --- FUNCIONES MODAL ---
function openCreateModal() {
  selectedTour.value = null;
  // Lógica futura para un modal que se controla con v-model
}

function openEditModal(tour: TourRes) {
  selectedTour.value = tour;
  // Lógica futura para un modal que se controla con v-model
}

function onModalSuccess() {
  refresh(); // Refresca los datos de la tabla
}

// --- ELIMINACIÓN ---
const toast = useToast();
async function handleDelete(tour: TourRes) {
  if (
    confirm(
      `¿Estás seguro de que quieres eliminar "${tour.nameTranslations?.es}"?`,
    )
  ) {
    try {
      if (!tour.id) return;
      await deleteAdminTour(tour.id);
      toast.add({ title: "Tour eliminado", color: "green" });
      await refresh();
    } catch (e: any) {
      toast.add({
        title: "Error al eliminar",
        description: e.message || "Error desconocido",
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
          <!-- Nombre del Slot: #<key>-cell -->
          <template #name-cell="{ row }">
            <span class="font-medium text-gray-900 dark:text-white">{{
              row.nameTranslations.es
            }}</span>
          </template>

          <template #category-cell="{ row }">
            <UBadge color="blue" variant="subtle" class="capitalize">
              {{ row.category.toLowerCase() }}
            </UBadge>
          </template>

          <template #priceAdult-cell="{ row }">
            <span>
              {{
                new Intl.NumberFormat("es-CL", {
                  style: "currency",
                  currency: "CLP",
                }).format(row.priceAdult)
              }}
            </span>
          </template>

          <template #status-cell="{ row }">
            <UBadge
              :color="
                row.status === 'PUBLISHED'
                  ? 'green'
                  : row.status === 'DRAFT'
                    ? 'yellow'
                    : 'red'
              "
              variant="subtle"
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

          <template #actions-cell="{ row }">
            <UDropdownMenu :items="items(row)">
              <UButton
                color="gray"
                variant="ghost"
                icon="i-lucide-more-horizontal"
              />
            </UDropdownMenu>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>
