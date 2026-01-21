<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
  tourOptions: Array<{ label: string, value: string | undefined }>
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const { assignMediaToTour } = useAdminData()

// State
const selectedTourId = ref<string | undefined>(undefined)

const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    if (!selectedTourId.value) {
      throw new Error('Selecciona un tour')
    }
    await assignMediaToTour(selectedTourId.value, props.mediaIds)
  },
  onSuccess: () => {
    selectedTourId.value = undefined
    emit('success')
  },
  successMessage: `${props.mediaIds.length} ${props.mediaIds.length === 1 ? 'medio asignado' : 'medios asignados'} al tour`,
  errorMessage: 'Error al asignar medios'
})

// Reset on open
watch(() => props.modelValue, (open) => {
  if (open) selectedTourId.value = undefined
})

// Filter out the "Sin asignar" option for bulk assign (only show actual tours)
const filteredTourOptions = computed(() =>
  props.tourOptions.filter(opt => opt.value !== undefined)
)
</script>

<template>
  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Asignar a Tour"
    :subtitle="`${mediaIds.length} ${mediaIds.length === 1 ? 'medio seleccionado' : 'medios seleccionados'}`"
    size="md"
    submit-label="Asignar"
    :submit-loading="isSubmitting"
    :submit-disabled="!selectedTourId"
    @submit="handleSubmit"
  >
    <div>
      <label class="block text-sm font-medium text-default mb-2">
        Tour
      </label>

      <USelect
        v-model="selectedTourId"
        :items="filteredTourOptions"
        option-attribute="label"
        value-attribute="value"
        placeholder="Selecciona un tour"
        size="lg"
        class="w-full"
      />

      <p class="text-xs text-muted mt-2">
        Las fotos se añadirán a la galería del tour seleccionado
      </p>
    </div>
  </AdminBaseAdminModal>
</template>
