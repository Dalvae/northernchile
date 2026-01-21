<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()
const { updateAdminMedia, fetchAdminMediaById } = useAdminData()

// State
const tags = ref<string[]>([])
const mode = ref<'add' | 'replace'>('add')

const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    if (tags.value.length === 0) {
      throw new Error('Añade al menos una etiqueta')
    }

    let updated = 0
    let failed = 0

    for (const mediaId of props.mediaIds) {
      try {
        let finalTags = tags.value

        // If adding, merge with existing tags
        if (mode.value === 'add') {
          const existingMedia = await fetchAdminMediaById(mediaId)
          const existingTags = existingMedia?.tags || []
          // Merge and deduplicate
          finalTags = [...new Set([...existingTags, ...tags.value])]
        }

        await updateAdminMedia(mediaId, { tags: finalTags })
        updated++
      } catch (error) {
        console.error(`Error updating media ${mediaId}:`, error)
        failed++
      }
    }

    if (failed > 0) {
      toast.add({
        color: 'warning',
        title: `${updated} actualizados, ${failed} fallaron`
      })
    }

    return { updated, failed }
  },
  onSuccess: () => {
    tags.value = []
    mode.value = 'add'
    emit('success')
  },
  successMessage: `Etiquetas ${mode.value === 'add' ? 'añadidas' : 'reemplazadas'} en ${props.mediaIds.length} ${props.mediaIds.length === 1 ? 'medio' : 'medios'}`,
  errorMessage: 'Error al actualizar etiquetas'
})

// Reset on open
watch(() => props.modelValue, (open) => {
  if (open) {
    tags.value = []
    mode.value = 'add'
  }
})
</script>

<template>
  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Editar Etiquetas"
    :subtitle="`${mediaIds.length} ${mediaIds.length === 1 ? 'medio seleccionado' : 'medios seleccionados'}`"
    size="md"
    :submit-label="mode === 'add' ? 'Añadir Etiquetas' : 'Reemplazar Etiquetas'"
    :submit-loading="isSubmitting"
    :submit-disabled="tags.length === 0"
    @submit="handleSubmit"
  >
    <div class="space-y-4">
      <!-- Mode selector -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Modo
        </label>

        <div class="flex gap-4">
          <label class="flex items-center gap-2 cursor-pointer">
            <URadio
              v-model="mode"
              value="add"
              name="tag-mode"
            />
            <span class="text-sm">Añadir a existentes</span>
          </label>
          <label class="flex items-center gap-2 cursor-pointer">
            <URadio
              v-model="mode"
              value="replace"
              name="tag-mode"
            />
            <span class="text-sm">Reemplazar todas</span>
          </label>
        </div>

        <p class="text-xs text-muted mt-2">
          {{ mode === 'add' ? 'Las etiquetas se añadirán a las existentes' : 'Las etiquetas existentes serán reemplazadas' }}
        </p>
      </div>

      <!-- Tags -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Etiquetas
        </label>
        <UiTagInput
          v-model="tags"
          lowercase
        />
      </div>
    </div>
  </AdminBaseAdminModal>
</template>
