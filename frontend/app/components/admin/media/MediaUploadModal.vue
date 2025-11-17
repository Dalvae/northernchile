<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const props = defineProps<{
  modelValue: boolean
  tourId?: string
  scheduleId?: string
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const toast = useToast()
const { uploadFile } = useS3Upload()
const authStore = useAuthStore()
const fileInput = ref<HTMLInputElement | null>(null)

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

// State
const uploadingFiles = ref<Array<{
  file: File
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  url?: string
  error?: string
}>>([])

const metadata = ref({
  altTranslations: { es: '', en: '', pt: '' },
  captionTranslations: { es: '', en: '', pt: '' },
  tags: [] as string[],
  takenAt: ''
})

const tagInput = ref('')

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !metadata.value.tags.includes(tag)) {
    metadata.value.tags.push(tag)
    tagInput.value = ''
  }
}

function removeTag(index: number) {
  metadata.value.tags.splice(index, 1)
}

function handleTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    event.preventDefault()
    addTag()
  }
}

// File handling
function onFilesSelected(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files) {
    processFiles(Array.from(target.files))
  }
}

function processFiles(files: File[]) {
  files.forEach(file => {
    // Validate
    if (!file.type.startsWith('image/')) {
      toast.add({ color: 'error', title: `Archivo inválido: ${file.name}. Solo se permiten imágenes.` })
      return
    }

    if (file.size > 10 * 1024 * 1024) {  // 10MB
      toast.add({ color: 'error', title: `Archivo muy grande: ${file.name}. Máximo 10MB.` })
      return
    }

    uploadingFiles.value.push({
      file,
      progress: 0,
      status: 'pending'
    })
  })
}

async function startUpload() {
  for (const item of uploadingFiles.value) {
    if (item.status !== 'pending') continue

    item.status = 'uploading'

    try {
      // Upload to S3
      const folder = props.tourId ? 'tours' : props.scheduleId ? 'schedules' : 'media'
      const result = await uploadFile(item.file, folder, (progress) => {
        item.progress = progress
      })

      if (!result) {
        throw new Error('Error al subir a S3 - verifica las credenciales de AWS')
      }

      // Create media record in DB
      await $fetch('/api/admin/media', {
        method: 'POST',
        body: {
          url: result.url,
          s3Key: result.key,
          tourId: props.tourId,
          scheduleId: props.scheduleId,
          originalFilename: item.file.name,
          sizeBytes: item.file.size,
          contentType: item.file.type,
          altTranslations: metadata.value.altTranslations,
          captionTranslations: metadata.value.captionTranslations,
          tags: metadata.value.tags,
          takenAt: metadata.value.takenAt ? new Date(metadata.value.takenAt).toISOString() : null
        },
        headers: headers.value
      })

      item.status = 'success'
      item.url = result.url
    } catch (error) {
      console.error('Upload error:', error)
      item.status = 'error'
      item.error = error.message || 'Error al subir'
    }
  }

  const successCount = uploadingFiles.value.filter(f => f.status === 'success').length

  if (successCount > 0) {
    toast.add({
      color: 'success',
      title: `${successCount} ${successCount === 1 ? 'foto subida' : 'fotos subidas'} con éxito`
    })
    emit('success')
  }

  // Reset or close
  const hasErrors = uploadingFiles.value.some(f => f.status === 'error')
  if (!hasErrors) {
    isOpen.value = false
    uploadingFiles.value = []
    metadata.value = {
      altTranslations: { es: '', en: '', pt: '' },
      captionTranslations: { es: '', en: '', pt: '' },
      tags: [],
      takenAt: ''
    }
  }
}

function removeFile(index: number) {
  uploadingFiles.value.splice(index, 1)
}

// Drag & drop
const isDragging = ref(false)

function onDrop(event: DragEvent) {
  event.preventDefault()
  isDragging.value = false

  if (event.dataTransfer?.files) {
    processFiles(Array.from(event.dataTransfer.files))
  }
}

function formatFileSize(bytes: number) {
  const kb = bytes / 1024
  if (kb < 1024) return `${kb.toFixed(1)} KB`
  const mb = kb / 1024
  return `${mb.toFixed(1)} MB`
}

function openFileDialog() {
  fileInput.value?.click()
}
</script>

<template>
  <UModal v-model:open="isOpen" :ui="{ width: 'sm:max-w-3xl' }">
    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex items-center justify-between pb-4 border-b border-neutral-200 dark:border-neutral-800">
          <h3 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
            Subir Fotos
          </h3>
          <UButton
            icon="i-heroicons-x-mark"
            variant="ghost"
            color="neutral"
            @click="isOpen = false"
          />
        </div>

        <!-- Content -->
        <div class="py-6 space-y-6 max-h-[70vh] overflow-y-auto">
        <!-- Drop zone -->
        <div
          class="border-2 border-dashed rounded-lg p-8 text-center transition-colors"
          :class="isDragging
            ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
            : 'border-neutral-300 dark:border-neutral-700'"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop="onDrop"
        >
          <UIcon
            name="i-heroicons-cloud-arrow-up"
            class="w-12 h-12 mx-auto mb-4 text-neutral-400"
          />

          <p class="text-lg font-medium text-neutral-900 dark:text-neutral-100 mb-2">
            Arrastra fotos aquí o haz clic para seleccionar
          </p>

          <p class="text-sm text-neutral-600 dark:text-neutral-400 mb-4">
            Máximo 10MB por archivo. Formatos: JPG, PNG, WEBP
          </p>

          <UButton color="primary" variant="soft" @click="openFileDialog">
            Seleccionar Archivos
          </UButton>

          <input
            ref="fileInput"
            type="file"
            multiple
            accept="image/*"
            class="hidden"
            @change="onFilesSelected"
          />
        </div>

        <!-- File list -->
        <div v-if="uploadingFiles.length > 0" class="space-y-3">
          <h4 class="font-medium text-neutral-900 dark:text-neutral-100">
            Archivos ({{ uploadingFiles.length }})
          </h4>

          <div class="space-y-2 max-h-64 overflow-y-auto">
            <div
              v-for="(item, index) in uploadingFiles"
              :key="index"
              class="flex items-center gap-3 p-3 bg-neutral-50 dark:bg-neutral-900 rounded-lg"
            >
              <!-- Preview -->
              <img
                v-if="item.status === 'success' && item.url"
                :src="item.url"
                class="w-12 h-12 object-cover rounded"
              />
              <div
                v-else
                class="w-12 h-12 bg-neutral-200 dark:bg-neutral-800 rounded flex items-center justify-center"
              >
                <UIcon name="i-heroicons-photo" class="w-6 h-6 text-neutral-400" />
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-neutral-900 dark:text-neutral-100 truncate">
                  {{ item.file.name }}
                </p>
                <p class="text-xs text-neutral-600 dark:text-neutral-400">
                  {{ formatFileSize(item.file.size) }}
                </p>

                <!-- Progress -->
                <UProgress
                  v-if="item.status === 'uploading'"
                  :value="item.progress"
                  size="xs"
                  class="mt-1"
                />

                <!-- Error -->
                <p v-if="item.status === 'error'" class="text-xs text-error-600 dark:text-error-400 mt-1">
                  {{ item.error }}
                </p>
              </div>

              <!-- Status -->
              <UBadge
                :color="item.status === 'success' ? 'success' : item.status === 'error' ? 'error' : 'neutral'"
                variant="soft"
              >
                {{ item.status === 'pending' ? 'Listo' : item.status === 'uploading' ? 'Subiendo' : item.status === 'success' ? 'Completado' : 'Error' }}
              </UBadge>

              <!-- Remove -->
              <UButton
                v-if="item.status !== 'uploading'"
                icon="i-heroicons-x-mark"
                variant="ghost"
                color="neutral"
                size="sm"
                @click="removeFile(index)"
              />
            </div>
          </div>
        </div>

        <!-- Metadata (common for all files) -->
        <div class="space-y-4 pt-4 border-t border-neutral-200 dark:border-neutral-800">
          <h4 class="font-medium text-neutral-900 dark:text-neutral-100">
            Metadatos (aplicado a todas las fotos)
          </h4>

          <!-- Alt text (multilingual tabs) -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Texto Alternativo (SEO)
            </label>
            <UTabs
              :items="[
                { label: 'ES', slot: 'es' },
                { label: 'EN', slot: 'en' },
                { label: 'PT', slot: 'pt' }
              ]"
            >
              <template #es>
                <UInput
                  v-model="metadata.altTranslations.es"
                  placeholder="Descripción de la imagen en español"
                  size="lg"
                  class="w-full"
                />
              </template>
              <template #en>
                <UInput
                  v-model="metadata.altTranslations.en"
                  placeholder="Image description in English"
                  size="lg"
                  class="w-full"
                />
              </template>
              <template #pt>
                <UInput
                  v-model="metadata.altTranslations.pt"
                  placeholder="Descrição da imagem em português"
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

            <!-- Tags display -->
            <div v-if="metadata.tags.length > 0" class="flex flex-wrap gap-2 mb-2">
              <UBadge
                v-for="(tag, index) in metadata.tags"
                :key="index"
                color="primary"
                variant="soft"
                class="cursor-pointer"
                @click="removeTag(index)"
              >
                {{ tag }}
                <UIcon name="i-heroicons-x-mark" class="w-3 h-3 ml-1" />
              </UBadge>
            </div>

            <!-- Tag input -->
            <UInput
              v-model="tagInput"
              placeholder="Añade etiquetas (ej: noche, estrellas, grupo)"
              size="lg"
              class="w-full"
              @keydown="handleTagKeydown"
            />
            <p class="text-xs text-neutral-600 dark:text-neutral-400 mt-1">
              Presiona Enter para agregar. Click en la etiqueta para eliminar.
            </p>
          </div>

          <!-- Photo date -->
          <div>
            <label class="block text-sm font-medium text-neutral-900 dark:text-neutral-100 mb-2">
              Fecha de la Foto (opcional)
            </label>
            <UInput
              v-model="metadata.takenAt"
              type="datetime-local"
              size="lg"
              class="w-full"
            />
            <p class="text-xs text-neutral-600 dark:text-neutral-400 mt-1">
              Si no se proporciona, se usará la fecha de subida
            </p>
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
            :disabled="uploadingFiles.length === 0"
            :loading="uploadingFiles.some(f => f.status === 'uploading')"
            @click="startUpload"
          >
            Subir {{ uploadingFiles.length }} {{ uploadingFiles.length === 1 ? 'Foto' : 'Fotos' }}
          </UButton>
        </div>
      </div>
    </template>
  </UModal>
</template>
