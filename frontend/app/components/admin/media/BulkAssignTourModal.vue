<script setup lang="ts">
import type { TourRes } from 'api-client'

const props = defineProps<{
  modelValue: boolean
  mediaIds: string[]
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()
const { fetchAdminTours, assignMediaToTour } = useAdminData()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// State
const selectedTourId = ref<string | undefined>(undefined)
const saving = ref(false)

// Fetch tours
const { data: tours, status } = useAsyncData<TourRes[]>(
  'tours-for-bulk-assign',
  () => fetchAdminTours(),
  {
    server: false,
    lazy: true,
    default: () => []
  }
)

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
    toast.add({ color: 'warning', title: 'Selecciona un tour' })
    return
  }

  saving.value = true
  try {
    await assignMediaToTour(selectedTourId.value, props.mediaIds)

    toast.add({
      color: 'success',
      title: `${props.mediaIds.length} ${props.mediaIds.length === 1 ? 'medio asignado' : 'medios asignados'} al tour`
    })
    emit('success')
    isOpen.value = false
  } catch (error) {
    console.error('Error assigning media to tour:', error)
    toast.add({ color: 'error', title: 'Error al asignar medios' })
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
              Asignar a Tour
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
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
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

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-2">
              Las fotos se añadirán a la galería del tour seleccionado
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
            :disabled="!selectedTourId"
            @click="save"
          >
            Asignar
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
