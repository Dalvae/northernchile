<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  confirmColor?: 'primary' | 'error' | 'warning'
  icon?: string
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue' | 'confirm' | 'cancel', value?: boolean): void
}>()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
  isOpen.value = false
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    class="sm:max-w-md"
  >
    <template #content>
      <div class="p-6">
        <div class="flex items-start gap-4">
          <!-- Icon -->
          <div
            v-if="icon"
            class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center"
            :class="{
              'bg-error-100 dark:bg-error-900/30': confirmColor === 'error',
              'bg-warning-100 dark:bg-warning-900/30': confirmColor === 'warning',
              'bg-primary-100 dark:bg-primary-900/30': confirmColor === 'primary' || !confirmColor
            }"
          >
            <UIcon
              :name="icon"
              class="w-5 h-5"
              :class="{
                'text-error-600 dark:text-error-400': confirmColor === 'error',
                'text-warning-600 dark:text-warning-400': confirmColor === 'warning',
                'text-primary-600 dark:text-primary-400': confirmColor === 'primary' || !confirmColor
              }"
            />
          </div>

          <!-- Content -->
          <div class="flex-1">
            <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
              {{ title }}
            </h3>
            <p class="mt-2 text-sm text-neutral-600 dark:text-neutral-300">
              {{ message }}
            </p>
          </div>
        </div>

        <!-- Actions -->
        <div class="mt-6 flex justify-end gap-3">
          <UButton
            variant="outline"
            color="neutral"
            :disabled="loading"
            @click="handleCancel"
          >
            {{ cancelLabel || 'Cancelar' }}
          </UButton>
          <UButton
            :color="confirmColor || 'primary'"
            :loading="loading"
            @click="handleConfirm"
          >
            {{ confirmLabel || 'Confirmar' }}
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
