<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()
const { updateAdminMedia, fetchAdminMediaById } = useAdminData()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// State
const tags = ref<string[]>([])
const tagInput = ref('')
const mode = ref<'add' | 'replace'>('add')
const saving = ref(false)

// Reset on open
watch(isOpen, (open) => {
  if (open) {
    tags.value = []
    tagInput.value = ''
    mode.value = 'add'
  }
})

function addTag() {
  const tag = tagInput.value.trim().toLowerCase()
  if (tag && !tags.value.includes(tag)) {
    tags.value.push(tag)
    tagInput.value = ''
  }
}

function removeTag(index: number) {
  tags.value.splice(index, 1)
}

function handleTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    event.preventDefault()
    addTag()
  }
}

async function save() {
  if (tags.value.length === 0) {
    toast.add({ color: 'warning', title: 'Añade al menos una etiqueta' })
    return
  }

  saving.value = true
  let updated = 0
  let failed = 0

  try {
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

    if (failed === 0) {
      toast.add({
        color: 'success',
        title: `Etiquetas ${mode.value === 'add' ? 'añadidas' : 'reemplazadas'} en ${updated} ${updated === 1 ? 'medio' : 'medios'}`
      })
    } else {
      toast.add({
        color: 'warning',
        title: `${updated} actualizados, ${failed} fallaron`
      })
    }

    emit('success')
    isOpen.value = false
  } catch (error) {
    console.error('Error updating tags:', error)
    toast.add({ color: 'error', title: 'Error al actualizar etiquetas' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    class="sm:max-w-md"
  >
    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex items-center justify-between pb-4 border-b border-neutral-200 dark:border-neutral-800">
          <div>
            <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
              Editar Etiquetas
            </h3>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">
              {{ mediaIds.length }} {{ mediaIds.length === 1 ? 'medio seleccionado' : 'medios seleccionados' }}
            </p>
          </div>
          <UButton
            icon="i-heroicons-x-mark"
            variant="ghost"
            color="neutral"
            @click="isOpen = false"
          />
        </div>

        <!-- Content -->
        <div class="py-6 space-y-4">
          <!-- Mode selector -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
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

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-2">
              {{ mode === 'add' ? 'Las etiquetas se añadirán a las existentes' : 'Las etiquetas existentes serán reemplazadas' }}
            </p>
          </div>

          <!-- Tags -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Etiquetas
            </label>

            <!-- Tags display -->
            <div
              v-if="tags.length > 0"
              class="flex flex-wrap gap-2 mb-2"
            >
              <UBadge
                v-for="(tag, index) in tags"
                :key="index"
                color="primary"
                variant="soft"
                class="cursor-pointer"
                @click="removeTag(index)"
              >
                {{ tag }}
                <UIcon
                  name="i-heroicons-x-mark"
                  class="w-3 h-3 ml-1"
                />
              </UBadge>
            </div>

            <!-- Tag input -->
            <UInput
              v-model="tagInput"
              placeholder="Escribe y presiona Enter"
              size="lg"
              class="w-full"
              @keydown="handleTagKeydown"
            />

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-1">
              Presiona Enter para agregar. Click en la etiqueta para eliminar.
            </p>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex justify-end gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-800">
          <UButton
            variant="outline"
            color="neutral"
            @click="isOpen = false"
          >
            Cancelar
          </UButton>

          <UButton
            color="primary"
            :loading="saving"
            :disabled="tags.length === 0"
            @click="save"
          >
            {{ mode === 'add' ? 'Añadir' : 'Reemplazar' }} Etiquetas
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
