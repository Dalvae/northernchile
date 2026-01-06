<script setup lang="ts">
import { h } from 'vue'
import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import type { TourRes } from 'api-client'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Tours - Admin - Northern Chile'
})

const { deleteAdminTour } = useAdminData()
const { formatPrice } = useCurrency()
const authStore = useAuthStore()

const adminStore = useAdminStore()

const {
  pending,
  refresh
} = useAdminToursData()

const tours = computed(() => adminStore.tours)

// ✅ Variables para controlar los modales
const isCreateModalOpen = ref(false)
const isEditModalOpen = ref(false)
const selectedTour = ref<TourRes | null>(null)
const q = ref('')
const columns = computed(() => {
  const baseColumns = [
    {
      id: 'name',
      accessorKey: 'nameTranslations.es',
      header: 'Nombre',
      cell: ({ row }: { row: { original: TourRes } }) =>
        h('span', { class: 'font-medium' }, row.original.nameTranslations?.es || 'Sin nombre')
    },
    {
      id: 'price',
      accessorKey: 'price',
      header: 'Precio',
      cell: ({ row }: { row: { original: TourRes } }) =>
        h('span', { class: 'font-semibold' }, formatPrice(row.original.price))
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
      header: 'Acciones',
      cell: ({ row }: { row: { original: TourRes } }) =>
        h('div', { class: 'flex items-center gap-2' }, [
          h(resolveComponent('UButton'), {
            'icon': 'i-lucide-pencil',
            'color': 'neutral',
            'variant': 'ghost',
            'size': 'sm',
            'aria-label': 'Editar tour',
            'onClick': () => openEditModal(row.original)
          }),
          h(resolveComponent('UButton'), {
            'icon': 'i-lucide-trash-2',
            'color': 'error',
            'variant': 'ghost',
            'size': 'sm',
            'aria-label': 'Eliminar tour',
            'onClick': () => handleDelete(row.original)
          })
        ])
    }
  ]

  // Insert Owner or Description at index 1
  if (authStore.isSuperAdmin) {
    baseColumns.splice(1, 0, {
      id: 'owner',
      header: 'Dueño',
      cell: ({ row }: { row: { original: TourRes } }) =>
        h('div', { class: 'flex flex-col' }, [
          h('span', { class: 'text-sm font-medium text-default' }, row.original.ownerName || 'Sin dueño'),
          h('span', { class: 'text-xs text-muted' }, row.original.ownerEmail)
        ])
    })
  } else {
    baseColumns.splice(1, 0, {
      id: 'description',
      header: 'Descripción',
      cell: ({ row }: { row: { original: TourRes } }) =>
        h('span', { class: 'text-sm text-default line-clamp-2' }, row.original.descriptionBlocksTranslations?.es?.[0]?.content || 'Sin descripción')
    })
  }

  return baseColumns
})

const filteredRows = computed(() => {
  if (!tours.value || tours.value.length === 0) return []

  let rows = tours.value

  if (q.value) {
    const query = q.value.toLowerCase()
    rows = rows.filter(tour =>
      tour.nameTranslations?.es?.toLowerCase().includes(query)
      || tour.ownerName?.toLowerCase().includes(query)
      || tour.ownerEmail?.toLowerCase().includes(query)
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
    } catch (e: unknown) {
      toast.add({
        title: 'Error al eliminar',
        description: e instanceof Error ? e.message : 'Error desconocido',
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
        />
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
