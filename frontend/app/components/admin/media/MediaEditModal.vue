<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const props = defineProps<{
  modelValue: boolean
  media: any | null
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()
const authStore = useAuthStore()

const headers = computed(() => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`
  }
  return headers
})

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// Form state
const state = ref({
  altTranslations: { es: '', en: '', pt: '' },
  captionTranslations: { es: '', en: '', pt: '' },
  tags: [],
  takenAt: ''
})

// Load media data
watch(() => props.media, (media) => {
  if (media) {
    state.value = {
      altTranslations: media.altTranslations || { es: '', en: '', pt: '' },
      captionTranslations: media.captionTranslations || { es: '', en: '', pt: '' },
      tags: media.tags || [],
      takenAt: media.takenAt ? formatForInput(media.takenAt) : ''
    }
  }
}, { immediate: true })

function formatForInput(isoString: string) {
  // Convert ISO to datetime-local format
  const date = new Date(isoString)
  const offset = date.getTimezoneOffset() * 60000
  const localDate = new Date(date.getTime() - offset)
  return localDate.toISOString().slice(0, 16)
}

async function save() {
  try {
    await $fetch(`/api/admin/media/${props.media.id}`, {
      method: 'PATCH',
      body: {
        altTranslations: state.value.altTranslations,
        captionTranslations: state.value.captionTranslations,
        tags: state.value.tags,
        takenAt: state.value.takenAt ? new Date(state.value.takenAt).toISOString() : null
      },
      headers: headers.value
    })

    toast.add({ color: 'success', title: 'Medio actualizado' })
    emit('success')
    isOpen.value = false
  } catch (error) {
    console.error('Error updating media:', error)
    toast.add({ color: 'error', title: 'Error al actualizar' })
  }
}

function formatFileSize(bytes) {
  if (!bytes) return '-'
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(1)} MB`
}

function formatDate(dateString) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('es-CL')
}

function getTypeLabel(type) {
  const labels = {
    TOUR: 'Tour',
    SCHEDULE: 'Programa',
    LOOSE: 'Suelto'
  }
  return labels[type] || type
}

function getTypeBadgeColor(type) {
  const colors = {
    TOUR: 'primary',
    SCHEDULE: 'secondary',
    LOOSE: 'neutral'
  }
  return colors[type] || 'neutral'
}
</script>

<template>
  <UModal v-model:open="isOpen" :ui="{ width: 'sm:max-w-2xl' }">
    <template #content>
      <!-- Header -->
      <div class="flex items-center justify-between pb-4 border-b border-neutral-200 dark:border-neutral-800">
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
          Editar Medio
        </h3>
        <UButton
          icon="i-heroicons-x-mark"
          variant="ghost"
          color="neutral"
          @click="isOpen = false"
        />
      </div>

      <!-- Content -->
      <div v-if="media" class="py-6 space-y-6">
        <!-- Preview -->
        <div class="flex gap-4">
          <img
            :src="media.url"
            :alt="media.altTranslations?.es"
            class="w-32 h-32 object-cover rounded-lg shadow-sm"
          />

          <div class="flex-1 space-y-2">
            <p class="font-medium text-neutral-900 dark:text-neutral-100">
              {{ media.originalFilename }}
            </p>
            <div class="flex gap-2 text-sm text-neutral-600 dark:text-neutral-400">
              <span>{{ formatFileSize(media.sizeBytes) }}</span>
              <span>•</span>
              <span>{{ formatDate(media.uploadedAt) }}</span>
            </div>
            <UBadge
              :color="getTypeBadgeColor(media.type)"
              variant="soft"
            >
              {{ getTypeLabel(media.type) }}
            </UBadge>
          </div>
        </div>

        <!-- Alt text (multilingual tabs) -->
        <div>
          <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Texto Alternativo (SEO)
          </label>

          <UTabs
            :items="[
              { label: 'Español', slot: 'es' },
              { label: 'English', slot: 'en' },
              { label: 'Português', slot: 'pt' }
            ]"
          >
            <template #es>
              <UInput
                v-model="state.altTranslations.es"
                placeholder="Descripción de la imagen"
                size="lg"
              />
            </template>
            <template #en>
              <UInput
                v-model="state.altTranslations.en"
                placeholder="Image description"
                size="lg"
              />
            </template>
            <template #pt>
              <UInput
                v-model="state.altTranslations.pt"
                placeholder="Descrição da imagem"
                size="lg"
              />
            </template>
          </UTabs>
        </div>

        <!-- Caption (multilingual tabs) -->
        <div>
          <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Leyenda (opcional)
          </label>

          <UTabs
            :items="[
              { label: 'ES', slot: 'es' },
              { label: 'EN', slot: 'en' },
              { label: 'PT', slot: 'pt' }
            ]"
          >
            <template #es>
              <UTextarea
                v-model="state.captionTranslations.es"
                placeholder="Leyenda o contexto adicional"
                :rows="2"
                size="lg"
              />
            </template>
            <template #en>
              <UTextarea
                v-model="state.captionTranslations.en"
                placeholder="Caption or additional context"
                :rows="2"
                size="lg"
              />
            </template>
            <template #pt>
              <UTextarea
                v-model="state.captionTranslations.pt"
                placeholder="Legenda ou contexto adicional"
                :rows="2"
                size="lg"
              />
            </template>
          </UTabs>
        </div>

        <!-- Tags -->
        <div>
          <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Etiquetas
          </label>

          <USelectMenu
            v-model="state.tags"
            :items="[]"
            multiple
            creatable
            searchable
            placeholder="Añade etiquetas para organizar"
            size="lg"
          />

          <p class="text-xs text-neutral-600 dark:text-neutral-400 mt-1">
            Útil para buscar y filtrar medios
          </p>
        </div>

        <!-- Photo date -->
        <div>
          <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Fecha de la Foto
          </label>

          <UInput
            v-model="state.takenAt"
            type="datetime-local"
            size="lg"
          />

          <p class="text-xs text-neutral-600 dark:text-neutral-400 mt-1">
            Fecha en que se tomó la fotografía
          </p>
        </div>

        <!-- EXIF data (read-only) -->
        <div v-if="media.exifData && Object.keys(media.exifData).length > 0">
          <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Datos EXIF
          </label>

          <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 text-sm space-y-1">
            <div v-for="(value, key) in media.exifData" :key="key" class="flex justify-between">
              <span class="text-neutral-600 dark:text-neutral-400">{{ key }}:</span>
              <span class="text-neutral-900 dark:text-neutral-100 font-mono">{{ value }}</span>
            </div>
          </div>
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
          @click="save"
        >
          Guardar Cambios
        </UButton>
      </div>
    </template>
  </UModal>
</template>
