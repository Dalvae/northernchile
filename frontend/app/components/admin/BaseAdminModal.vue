<script setup lang="ts">
/**
 * BaseAdminModal - Reusable modal wrapper for admin panels
 * Provides consistent styling with header, scrollable content, and footer
 */

const {
  title,
  subtitle,
  size = 'md',
  contentHeight = '60vh',
  submitLabel = 'Guardar',
  cancelLabel = 'Cancelar',
  submitLoading = false,
  submitDisabled = false,
  hideFooter = false,
  hideCancel = false
} = defineProps<{
  title: string
  subtitle?: string
  size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl'
  contentHeight?: string
  submitLabel?: string
  cancelLabel?: string
  submitLoading?: boolean
  submitDisabled?: boolean
  hideFooter?: boolean
  hideCancel?: boolean
}>()

const emit = defineEmits<{
  submit: []
  cancel: []
}>()

const isOpen = defineModel<boolean>('open', { default: false })

const sizeClasses: Record<string, string> = {
  'sm': 'sm:max-w-sm',
  'md': 'sm:max-w-md',
  'lg': 'sm:max-w-lg',
  'xl': 'sm:max-w-xl',
  '2xl': 'sm:max-w-2xl'
}

function close() {
  isOpen.value = false
  emit('cancel')
}

function handleSubmit() {
  emit('submit')
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    :class="sizeClasses[size]"
  >
    <template v-if="$slots.trigger" #default>
      <slot name="trigger" />
    </template>

    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-start pb-4 border-b border-default">
          <div>
            <h3 class="text-xl font-semibold text-default">
              {{ title }}
            </h3>
            <p
              v-if="subtitle"
              class="text-sm text-muted mt-1"
            >
              {{ subtitle }}
            </p>
          </div>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="sm"
            aria-label="Cerrar"
            @click="close"
          />
        </div>

        <!-- Content -->
        <div
          class="py-4 overflow-y-auto"
          :style="{ maxHeight: contentHeight }"
        >
          <slot />
        </div>

        <!-- Footer -->
        <div
          v-if="!hideFooter"
          class="flex justify-end gap-3 pt-4 border-t border-default"
        >
          <slot name="footer">
            <UButton
              v-if="!hideCancel"
              :label="cancelLabel"
              color="neutral"
              variant="outline"
              :disabled="submitLoading"
              @click="close"
            />
            <UButton
              :label="submitLabel"
              color="primary"
              :loading="submitLoading"
              :disabled="submitDisabled"
              @click="handleSubmit"
            />
          </slot>
        </div>
      </div>
    </template>
  </UModal>
</template>
