<script setup lang="ts">
import type { TourRes } from '~/lib/api-client';

definePageMeta({
  layout: 'admin'
});

const { fetchAdminTours, deleteAdminTour } = useAdminData();
const { data: tours, pending, error, refresh } = await fetchAdminTours();

const q = ref('');
const isSlideoverOpen = ref(false);
const selectedTour = ref<TourRes | null>(null);

const columns = [
  { key: 'name', label: 'Nombre', sortable: true },
  { key: 'category', label: 'Categoría', sortable: true },
  { key: 'priceAdult', label: 'Precio Adulto', sortable: true },
  { key: 'status', label: 'Estado', sortable: true },
  { key: 'actions', label: 'Acciones' }
];

const filteredRows = computed(() => {
  if (!tours.value) return [];
  if (!q.value) return tours.value;
  return tours.value.filter(tour =>
    tour.name.toLowerCase().includes(q.value.toLowerCase())
  );
});

const items = (row: TourRes) => [
  [{
    label: 'Editar',
    icon: 'i-lucide-pencil',
    click: () => openEditSlideover(row)
  }],
  [{
    label: 'Eliminar',
    icon: 'i-lucide-trash-2',
    click: () => handleDelete(row)
  }]
];

function openCreateSlideover() {
  selectedTour.value = null;
  isSlideoverOpen.value = true;
}

function openEditSlideover(tour: TourRes) {
  selectedTour.value = tour;
  isSlideoverOpen.value = true;
}

const toast = useToast();
async function handleDelete(tour: TourRes) {
  if (confirm(`¿Estás seguro de que quieres eliminar el tour "${tour.name}"?`)) {
    try {
      await deleteAdminTour(tour.id!); // Assuming tour.id is not null
      toast.add({ title: 'Tour eliminado', color: 'green' });
      refresh();
    } catch (e: any) {
      toast.add({ title: 'Error al eliminar', description: e.message, color: 'red' });
    }
  }
}

function onActionSuccess() {
  refresh();
}

</script>

<template>
  <div>
    <UPageHeader title="Gestión de Tours">
      <template #right>
        <UInput ref="input" v-model="q" icon="i-lucide-search" placeholder="Buscar tour..." class="w-64" />
        <UButton label="Crear Tour" trailing-icon="i-lucide-plus" color="primary" @click="openCreateSlideover" />
      </template>
    </UPageHeader>

    <UPageBody>
      <UTable
        :rows="filteredRows"
        :columns="columns"
        :loading="pending"
        :empty-state="{ icon: 'i-lucide-map', label: 'No hay tours.' }"
      >
        <template #priceAdult-data="{ row }">
          <span>{{ new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(row.priceAdult) }}</span>
        </template>
        <template #status-data="{ row }">
          <UBadge
            :color="row.status === 'PUBLISHED' ? 'green' : 'gray'"
            variant="subtle"
          >
            {{ row.status }}
          </UBadge>
        </template>
        <template #actions-data="{ row }">
          <UDropdown :items="items(row)">
            <UButton color="gray" variant="ghost" icon="i-lucide-more-horizontal" />
          </UDropdown>
        </template>
      </UTable>
    </UPageBody>

    <AdminTourSlideover v-model="isSlideoverOpen" :tour="selectedTour" @success="onActionSuccess" />
  </div>
</template>