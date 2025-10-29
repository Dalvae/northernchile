<script setup lang="ts">
import { h, resolveComponent } from "vue";
import type { TourRes } from "~/lib/api-client";

definePageMeta({
  layout: "admin",
});

const { fetchAdminTours, deleteAdminTour } = useAdminData();

const {
  data: tours,
  pending,
  refresh,
} = useAsyncData("admin-tours", () => fetchAdminTours());

// ✅ Variables para controlar los modales
const isCreateModalOpen = ref(false);
const isEditModalOpen = ref(false);
const selectedTour = ref<TourRes | null>(null);
const q = ref("");

const columns = [
  {
    id: "name",
    accessorKey: "nameTranslations.es",
    header: "Nombre",
  },
  {
    id: "description",
    accessorKey: "descriptionTranslations.es",
    header: "Descripción",
  },
  {
    id: "priceAdult",
    accessorKey: "priceAdult",
    header: "Precio Adulto",
  },
  {
    id: "priceChild",
    accessorKey: "priceChild",
    header: "Precio Niño",
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

const filteredRows = computed(() => {
  if (!tours.value || tours.value.length === 0) return [];

  let rows = tours.value;

  if (q.value) {
    const query = q.value.toLowerCase();
    rows = rows.filter((tour) =>
      tour.nameTranslations?.es?.toLowerCase().includes(query),
    );
  }

  return rows;
});

// ✅ Abrir modal de creación
function openCreateModal() {
  selectedTour.value = null;
  isCreateModalOpen.value = true;
}

// ✅ Abrir modal de edición
function openEditModal(tour: TourRes) {
  selectedTour.value = tour;
  isEditModalOpen.value = true;
}

// ✅ Cerrar ambos modales y limpiar
function closeModals() {
  isCreateModalOpen.value = false;
  isEditModalOpen.value = false;
  selectedTour.value = null;
}

// ✅ Manejar éxito (crear o editar)
function onModalSuccess() {
  closeModals();
  refresh();
}

const toast = useToast();
async function handleDelete(tour: TourRes) {
  const tourName = tour.nameTranslations?.es || "este tour";
  if (confirm(`¿Estás seguro de que quieres eliminar "${tourName}"?`)) {
    try {
      await deleteAdminTour(tour.id);
      toast.add({
        title: "Tour eliminado",
        color: "green",
        icon: "i-lucide-check-circle",
      });
      await refresh();
    } catch (e: any) {
      toast.add({
        title: "Error al eliminar",
        description: e.message || "Error desconocido",
        color: "red",
        icon: "i-lucide-x-circle",
      });
    }
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
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
              placeholder="Buscar tour..."
              class="w-80"
            />
            <!-- ✅ Botón para abrir modal de CREAR -->
            <UButton
              label="Agregar Tour"
              trailing-icon="i-lucide-plus"
              color="primary"
              @click="openCreateModal"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div
        class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden"
      >
        <UTable
          :data="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-map',
            label: 'No hay tours creados.',
          }"
        >
          <template #name-data="{ row }">
            <span class="font-medium">
              {{ row.getValue("name") || "Sin nombre" }}
            </span>
          </template>

          <template #description-data="{ row }">
            <span class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
              {{ row.getValue("description") || "Sin descripción" }}
            </span>
          </template>

          <template #priceAdult-data="{ row }">
            <span class="font-semibold">
              {{
                new Intl.NumberFormat("es-CL", {
                  style: "currency",
                  currency: "CLP",
                }).format(row.getValue("priceAdult") || 0)
              }}
            </span>
          </template>

          <template #priceChild-data="{ row }">
            <span v-if="row.getValue('priceChild')">
              {{
                new Intl.NumberFormat("es-CL", {
                  style: "currency",
                  currency: "CLP",
                }).format(row.getValue("priceChild"))
              }}
            </span>
            <span v-else class="text-gray-400">N/A</span>
          </template>

          <template #status-data="{ row }">
            <UBadge
              :color="
                row.getValue('status') === 'PUBLISHED' ? 'green' : 'yellow'
              "
              variant="subtle"
            >
              {{
                row.getValue("status") === "PUBLISHED"
                  ? "Publicado"
                  : "Borrador"
              }}
            </UBadge>
          </template>

          <!-- ✅ Acciones con dos botones separados -->
          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-pencil"
                color="gray"
                variant="ghost"
                size="sm"
                aria-label="Editar tour"
                @click="openEditModal(row.original)"
              />

              <UButton
                icon="i-lucide-trash-2"
                color="red"
                variant="ghost"
                size="sm"
                aria-label="Eliminar tour"
                @click="handleDelete(row.original)"
              />
            </div>
          </template>
        </UTable>
      </div>
    </div>

    <!-- ✅ Modal de CREAR (sin tour) -->
    <AdminToursTourModal
      v-model:open="isCreateModalOpen"
      :tour="null"
      @success="onModalSuccess"
      @close="closeModals"
    />

    <!-- ✅ Modal de EDITAR (con tour seleccionado) -->
    <AdminToursTourModal
      v-model:open="isEditModalOpen"
      :tour="selectedTour"
      @success="onModalSuccess"
      @close="closeModals"
    />
  </div>
</template>
