<script setup lang="ts">
import type { TourRes } from 'api-client'
import { useAdminTourForm } from '~/composables/useAdminTourForm'
import TourFormGeneral from './form/General.vue'
import TourFormContent from './form/Content.vue'

const props = defineProps<{
  tour?: TourRes | null
  open?: boolean
}>()

const emit = defineEmits<{
  'success': []
  'close': []
  'update:open': [value: boolean]
}>()

const isEditing = computed(() => !!props.tour)

const { state, schema, loading, formErrors, onSubmit, onError, hasDraft, discardDraft }
  = useAdminTourForm(props, emit)

provide('tour-form-state', state)
provide('tour-form-errors', formErrors)

const isOpen = computed({
  get: () => props.open ?? false,
  set: value => emit('update:open', value)
})

const form = ref()

const handleSubmit = () => {
  form.value?.submit()
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    :title="isEditing ? 'Editar Tour' : 'Crear Nuevo Tour'"
    :ui="{
      content: 'max-w-5xl',
      body: 'max-h-[70vh] overflow-y-auto'
    }"
  >
    <template #body>
      <div class="py-4">
        <!-- Draft restored banner - only show option to discard -->
        <div
          v-if="hasDraft && !isEditing"
          class="mb-6 p-3 bg-info-50 dark:bg-info-950 border border-info-200 dark:border-info-800 rounded-lg"
        >
          <div class="flex items-center justify-between gap-4">
            <div class="flex items-center gap-2">
              <UIcon
                name="i-heroicons-document-text"
                class="text-info-600 dark:text-info-400 size-4"
              />
              <p class="text-sm text-info-700 dark:text-info-300">
                Borrador restaurado automáticamente
              </p>
            </div>
            <UButton
              label="Empezar de cero"
              size="xs"
              color="neutral"
              variant="ghost"
              @click="discardDraft"
            />
          </div>
        </div>

        <UForm
          ref="form"
          :schema="schema"
          :state="state"
          class="space-y-8"
          @submit="onSubmit"
          @error="onError"
        >
          <TourFormGeneral />
          <USeparator />
          <TourFormContent />
        </UForm>
      </div>
    </template>

    <template #footer>
      <div class="flex justify-between items-center w-full">
        <span class="text-sm text-muted">
          Revisa los campos antes de guardar
        </span>
        <div class="flex gap-3">
          <UButton
            label="Cancelar"
            color="neutral"
            variant="ghost"
            @click="emit('close')"
          />
          <UButton
            :label="isEditing ? 'Guardar Cambios' : 'Crear Tour'"
            :loading="loading"
            color="primary"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>
