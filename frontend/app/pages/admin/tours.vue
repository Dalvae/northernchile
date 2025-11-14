<script setup lang="ts">
import { h, resolveComponent } from 'vue'
import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import type { TourRes } from 'api-client'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Tours - Admin - Northern Chile'
})

const { fetchAdminTours, deleteAdminTour } = useAdminData()
const { formatPrice } = useCurrency()

const {
  data: tours,
  pending,
  refresh
} = useAsyncData<TourRes[]>('admin-tours', () => fetchAdminTours(), {
  server: false,
  lazy: true,
  default: () => []
})

// ✅ Variables para controlar los modales
const isCreateModalOpen = ref(false)
const isEditModalOpen = ref(false)
const selectedTour = ref<TourRes | null>(null)
const q = ref('')

const columns = [
  {
    id: 'name',
    accessorKey: 'nameTranslations.es',
    header: 'Nombre'
  },
  {
    id: 'description',
    accessorKey: 'descriptionTranslations.es',
    header: 'Descripción'
  },
  {
    id: 'price',
    accessorKey: 'price',
    header: 'Precio'
  },
  {
    id: 'status',
    accessorKey: 'status',
    header: 'Estado',
    cell: ({ row }: { row: { original: TourRes } }) =>
      h(AdminStatusBadge, {
        type: 'tour',
        status: row.original.status || 'DRAFT'
      })
  },
  {
    id: 'actions',
    header: 'Acciones'
  }
]

const filteredRows = computed(() => {
  if (!tours.value || tours.value.length === 0) return []

  let rows = tours.value

  if (q.value) {
    const query = q.value.toLowerCase()
    rows = rows.filter(tour =>
      tour.nameTranslations?.es?.toLowerCase().includes(query)
    )
  }

  return rows
})

// ✅ Abrir modal de creación
function openCreateModal() {
  selectedTour.value = null
  isCreateModalOpen.value = true
}

// ✅ Abrir modal de edición
function openEditModal(tour: TourRes) {
  selectedTour.value = tour
  isEditModalOpen.value = true
}

// ✅ Cerrar ambos modales y limpiar
function closeModals() {
  isCreateModalOpen.value = false
  isEditModalOpen.value = false
  selectedTour.value = null
}

// ✅ Manejar éxito (crear o editar)
function onModalSuccess() {
  closeModals()
  refresh()
}

const toast = useToast()
async function handleDelete(tour: TourRes) {
  const tourName = tour.nameTranslations?.es || 'este tour'
  if (confirm(`¿Estás seguro de que quieres eliminar "${tourName}"?`)) {
    try {
      if (!tour.id) throw new Error('Missing tour id')
      await deleteAdminTour(tour.id)
      toast.add({
        title: 'Tour eliminado',
        color: 'success',
        icon: 'i-lucide-check-circle'
      })
      await refresh()
    } catch (e: any) {
      toast.add({
        title: 'Error al eliminar',
        description: e.message || 'Error desconocido',
        color: 'error',
        icon: 'i-lucide-x-circle'
      })
    }
  }
}
</script>

<template>
  <div class="min-h-screen bg-default">
    <div class="p-6 space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-default">
            Gestión de Tours
          </h1>
          <p class="mt-1 text-muted">
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

      <!-- Table Container -->
      <div
        class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
      >
         <UTable
          :ui="{
            td: 'p-4 text-sm text-default whitespace-nowrap [&:has([role=checkbox])]:pe-0'
          }"
          :data="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-map',
            label: 'No hay tours creados.'
          }"
        >
          <template #name-data="{ row }">
            <span class="font-medium">
              {{ row.getValue("name") || "Sin nombre" }}
            </span>
          </template>

          <template #description-data="{ row }">
            <span
               class="text-sm text-default line-clamp-2"
            >
              {{ row.getValue("description") || "Sin descripción" }}
            </span>
          </template>

          <template #price-data="{ row }">
            <span class="font-semibold">
              {{ formatPrice(row.getValue("price") || 0) }}
            </span>
          </template>


          <!-- ✅ Acciones con dos botones separados -->
          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-pencil"
                color="neutral"
                variant="ghost"
                size="sm"
                aria-label="Editar tour"
                @click="openEditModal(row.original)"
              />

              <UButton
                icon="i-lucide-trash-2"
                color="error"
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
