<script setup lang="ts">
import type { MediaRes } from 'api-client'
import { formatFileSize, formatDateTime, getMediaTypeLabel, getMediaTypeBadgeColor, formatForDateTimeInput } from '~/utils/media'

const props = defineProps<{
  modelValue: boolean
  media: MediaRes | null
  tourOptions: Array<{ label: string; value: string | undefined }>
  scheduleOptions: Array<{ label: string; value: string | undefined }>
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const { updateAdminMedia } = useAdminData()

// Form state
const state = ref({
  altTranslations: { es: '', en: '', pt: '' } as Record<string, string>,
  captionTranslations: { es: '', en: '', pt: '' } as Record<string, string>,
  tags: [] as string[],
  takenAt: '',
  tourId: undefined as string | undefined,
  scheduleId: undefined as string | undefined
})

// Load media data
watch(() => props.media, (media) => {
  if (media) {
    state.value = {
      altTranslations: media.altTranslations || { es: '', en: '', pt: '' },
      captionTranslations: media.captionTranslations || { es: '', en: '', pt: '' },
      tags: media.tags || [],
      takenAt: media.takenAt ? formatForDateTimeInput(media.takenAt) : '',
      tourId: media.tourId || undefined,
      scheduleId: media.scheduleId || undefined
    }
  }
}, { immediate: true })

const { isOpen, isSubmitting, handleSubmit } = useControlledModalForm({
  modelValue: toRef(props, 'modelValue'),
  onUpdateModelValue: v => emit('update:modelValue', v),
  onSubmit: async () => {
    if (!props.media?.id) return
    await updateAdminMedia(props.media.id, {
      altTranslations: state.value.altTranslations,
      captionTranslations: state.value.captionTranslations,
      tags: state.value.tags,
      takenAt: state.value.takenAt ? new Date(state.value.takenAt).toISOString() : undefined,
      tourId: state.value.tourId,
      scheduleId: state.value.scheduleId
    })
  },
  onSuccess: () => {
    emit('success')
  },
  successMessage: 'Medio actualizado',
  errorMessage: 'Error al actualizar'
})
</script>

<template>
  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Editar Medio"
    size="2xl"
    content-height="70vh"
    submit-label="Guardar Cambios"
    :submit-loading="isSubmitting"
    @submit="handleSubmit"
  >
    <div
      v-if="media"
      class="space-y-6"
    >
      <!-- Preview -->
      <div class="flex gap-4">
        <NuxtImg
          :src="media.url"
          :alt="media.altTranslations?.es"
          class="w-32 h-32 object-cover rounded-lg shadow-sm"
          format="webp"
          loading="lazy"
          placeholder
        />

        <div class="flex-1 space-y-2">
          <p class="font-medium text-default">
            {{ media.altTranslations?.es || media.originalFilename }}
          </p>
          <p class="text-sm text-muted">
            {{ media.originalFilename }}
          </p>
          <div class="flex gap-2 text-sm text-muted">
            <span>{{ formatFileSize(media.sizeBytes) }}</span>
            <span>•</span>
            <span>{{ formatDateTime(media.uploadedAt) }}</span>
          </div>
          <UBadge
            :color="getMediaTypeBadgeColor(media.type)"
            variant="soft"
          >
            {{ getMediaTypeLabel(media.type) }}
          </UBadge>
        </div>
      </div>

      <!-- Alt text (multilingual tabs) -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
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
              class="w-full"
            />
          </template>
          <template #en>
            <UInput
              v-model="state.altTranslations.en"
              placeholder="Image description"
              size="lg"
              class="w-full"
            />
          </template>
          <template #pt>
            <UInput
              v-model="state.altTranslations.pt"
              placeholder="Descrição da imagem"
              size="lg"
              class="w-full"
            />
          </template>
        </UTabs>
      </div>

      <!-- Caption (multilingual tabs) -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
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
              class="w-full"
            />
          </template>
          <template #en>
            <UTextarea
              v-model="state.captionTranslations.en"
              placeholder="Caption or additional context"
              :rows="2"
              size="lg"
              class="w-full"
            />
          </template>
          <template #pt>
            <UTextarea
              v-model="state.captionTranslations.pt"
              placeholder="Legenda ou contexto adicional"
              :rows="2"
              size="lg"
              class="w-full"
            />
          </template>
        </UTabs>
      </div>

      <!-- Tags -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Etiquetas
        </label>
        <UiTagInput
          v-model="state.tags"
          placeholder="Añade etiquetas para organizar"
          lowercase
        />
      </div>

      <!-- Photo date -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Fecha de la Foto
        </label>

        <UInput
          v-model="state.takenAt"
          type="datetime-local"
          size="lg"
          class="w-full"
        />

        <p class="text-xs text-muted mt-1">
          Fecha en que se tomó la fotografía
        </p>
      </div>

      <!-- Assign to Tour -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Asignar a Tour
        </label>

        <USelect
          v-model="state.tourId"
          :items="props.tourOptions"
          option-attribute="label"
          value-attribute="value"
          placeholder="Selecciona un tour"
          size="lg"
          class="w-full"
        />

        <p class="text-xs text-muted mt-1">
          Asocia esta imagen a un tour específico
        </p>
      </div>

      <!-- Assign to Schedule -->
      <div>
        <label class="block text-sm font-medium text-default mb-2">
          Asignar a Salida
        </label>

        <USelect
          v-model="state.scheduleId"
          :items="props.scheduleOptions"
          option-attribute="label"
          value-attribute="value"
          placeholder="Selecciona una salida"
          size="lg"
          class="w-full"
        />

        <p class="text-xs text-muted mt-1">
          Asocia esta imagen a una salida específica de un tour
        </p>
      </div>

      <!-- EXIF data (read-only) -->
      <div v-if="media.exifData && Object.keys(media.exifData).length > 0">
        <label class="block text-sm font-medium text-default mb-2">
          Datos EXIF
        </label>

        <div class="bg-elevated rounded-lg p-4 text-sm space-y-1">
          <div
            v-for="(value, key) in media.exifData"
            :key="key"
            class="flex justify-between"
          >
            <span class="text-muted">{{ key }}:</span>
            <span class="text-default font-mono">{{ value }}</span>
          </div>
        </div>
      </div>
    </div>
  </AdminBaseAdminModal>
</template>
