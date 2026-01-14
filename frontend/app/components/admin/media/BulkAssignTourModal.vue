<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const { showSuccessToast, showErrorToast } = useApiError()
const { assignMediaToTour } = useAdminData()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// State
const selectedTourId = ref<string | undefined>(undefined)
const saving = ref(false)

const adminStore = useAdminStore()

// Fetch tours
const { status } = useAdminToursData()

const tours = computed(() => adminStore.tours)

const tourOptions = computed(() =>
  tours.value?.map(t => ({
    label: t.nameTranslations?.es || 'Sin nombre',
    value: t.id
  })) || []
)

// Reset on open
watch(isOpen, (open) => {
  if (open) {
    selectedTourId.value = undefined
  }
})

async function save() {
  if (!selectedTourId.value) {
    showErrorToast({ message: 'Selecciona un tour' })
    return
  }

  saving.value = true
  try {
    await assignMediaToTour(selectedTourId.value, props.mediaIds)

    showSuccessToast(`${props.mediaIds.length} ${props.mediaIds.length === 1 ? 'medio asignado' : 'medios asignados'} al tour`)
    emit('success')
    isOpen.value = false
  } catch (error) {
    console.error('Error assigning media to tour:', error)
    showErrorToast(error, 'Error al asignar medios')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Asignar a Tour"
    :subtitle="`${mediaIds.length} ${mediaIds.length === 1 ? 'medio seleccionado' : 'medios seleccionados'}`"
    size="md"
    submit-label="Asignar"
    :submit-loading="saving"
    :submit-disabled="!selectedTourId"
    @submit="save"
  >
    <div>
      <label class="block text-sm font-medium text-default mb-2">
        Tour
      </label>

      <USelect
        v-model="selectedTourId"
        :items="tourOptions"
        option-attribute="label"
        value-attribute="value"
        placeholder="Selecciona un tour"
        size="lg"
        class="w-full"
        :loading="status === 'pending'"
      />

      <p class="text-xs text-muted mt-2">
        Las fotos se añadirán a la galería del tour seleccionado
      </p>
    </div>
  </AdminBaseAdminModal>
</template>
