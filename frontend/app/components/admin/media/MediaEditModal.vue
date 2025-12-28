<script setup lang="ts">
import type { MediaRes } from 'api-client'
import { formatFileSize, formatDateTime, getMediaTypeLabel, getMediaTypeBadgeColor, formatForDateTimeInput } from '~/utils/media'

const props = defineProps<{
  modelValue: boolean
  media: MediaRes | null
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// Form state
const state = ref({
  altTranslations: { es: '', en: '', pt: '' },
  captionTranslations: { es: '', en: '', pt: '' },
  tags: [] as string[],
  takenAt: '',
  tourId: undefined as string | undefined,
  scheduleId: undefined as string | undefined
})

// Fetch tours for assignment
const { fetchAdminTours, fetchAdminSchedules, updateAdminMedia } = useAdminData()
const { data: tours } = useAsyncData('tours-for-media', () => fetchAdminTours(), {
  server: false,
  lazy: true,
  default: () => []
})

const { data: schedules } = useAsyncData('schedules-for-media', () => fetchAdminSchedules({ mode: 'past' }), {
  server: false,
  lazy: true,
  default: () => []
})

const tourOptions = computed(() => [
  { label: 'Sin asignar', value: undefined },
  ...(tours.value?.map(t => ({ label: t.nameTranslations?.es || 'Sin nombre', value: t.id })) || [])
])

const scheduleOptions = computed(() => [
  { label: 'Sin asignar', value: undefined },
  ...(schedules.value?.map(s => ({
    label: `${s.tourName} - ${s.startDatetime ? new Date(s.startDatetime).toLocaleDateString('es-CL') : ''}`,
    value: s.id
  })) || [])
])

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

async function save() {
  try {
    await updateAdminMedia(props.media.id, {
      altTranslations: state.value.altTranslations,
      captionTranslations: state.value.captionTranslations,
      tags: state.value.tags,
      takenAt: state.value.takenAt ? new Date(state.value.takenAt).toISOString() : undefined,
      tourId: state.value.tourId,
      scheduleId: state.value.scheduleId
    })

    toast.add({ color: 'success', title: 'Medio actualizado' })
    emit('success')
    isOpen.value = false
  } catch (error) {
    console.error('Error updating media:', error)
    toast.add({ color: 'error', title: 'Error al actualizar' })
  }
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    class="sm:max-w-2xl"
  >
    <template #content>
      <div class="p-6">
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
        <div
          v-if="media"
          class="py-6 space-y-6 max-h-[70vh] overflow-y-auto"
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
              <p class="font-medium text-neutral-900 dark:text-neutral-100">
                {{ media.altTranslations?.es || media.originalFilename }}
              </p>
              <p class="text-sm text-neutral-600 dark:text-neutral-300">
                {{ media.originalFilename }}
              </p>
              <div class="flex gap-2 text-sm text-neutral-600 dark:text-neutral-300">
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
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
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
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Fecha de la Foto
            </label>

            <UInput
              v-model="state.takenAt"
              type="datetime-local"
              size="lg"
              class="w-full"
            />

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-1">
              Fecha en que se tomó la fotografía
            </p>
          </div>

          <!-- Assign to Tour -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Asignar a Tour
            </label>

            <USelect
              v-model="state.tourId"
              :items="tourOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Selecciona un tour"
              size="lg"
              class="w-full"
            />

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-1">
              Asocia esta imagen a un tour específico
            </p>
          </div>

          <!-- Assign to Schedule -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Asignar a Salida
            </label>

            <USelect
              v-model="state.scheduleId"
              :items="scheduleOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Selecciona una salida"
              size="lg"
              class="w-full"
            />

            <p class="text-xs text-neutral-600 dark:text-neutral-300 mt-1">
              Asocia esta imagen a una salida específica de un tour
            </p>
          </div>

          <!-- EXIF data (read-only) -->
          <div v-if="media.exifData && Object.keys(media.exifData).length > 0">
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Datos EXIF
            </label>

            <div class="bg-neutral-50 dark:bg-neutral-900 rounded-lg p-4 text-sm space-y-1">
              <div
                v-for="(value, key) in media.exifData"
                :key="key"
                class="flex justify-between"
              >
                <span class="text-neutral-600 dark:text-neutral-300">{{ key }}:</span>
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
      </div>
    </template>
  </UModal>
</template>
