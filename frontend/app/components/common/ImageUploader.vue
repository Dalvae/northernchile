<template>
  <div class="space-y-4">
    <!-- Preview de imagen actual -->
    <div
      v-if="modelValue || previewUrl"
      class="relative"
    >
      <img
        :src="previewUrl || modelValue"
        alt="Preview"
        class="w-full h-48 object-cover rounded-lg border border-neutral-700"
      >
      <UButton
        icon="i-heroicons-x-mark"
        color="error"
        size="sm"
        class="absolute top-2 right-2"
        @click="handleRemove"
      />
    </div>

    <!-- Zona de carga -->
    <div
      class="border-2 border-dashed border-neutral-700 rounded-lg p-8 text-center cursor-pointer hover:border-primary transition-colors"
      :class="{ 'border-primary bg-primary/5': isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileSelect"
      >

      <div
        v-if="!isOptimizing && !isUploading"
        class="space-y-2"
      >
        <div class="flex justify-center">
          <UIcon
            name="i-heroicons-cloud-arrow-up"
            class="w-12 h-12 text-neutral-500"
          />
        </div>
        <p class="text-neutral-300">
          {{ isDragging ? 'Suelta la imagen aquí' : 'Arrastra una imagen o haz clic para seleccionar' }}
        </p>
        <p class="text-sm text-neutral-500">
          PNG, JPG, GIF (se convertirá a WebP, límite 10MB después de optimizar)
        </p>
      </div>

      <div
        v-else-if="isOptimizing"
        class="space-y-2"
      >
        <div class="flex justify-center">
          <UIcon
            name="i-heroicons-sparkles"
            class="w-12 h-12 text-primary-500 animate-pulse"
          />
        </div>
        <p class="text-sm text-neutral-300">
          Optimizando imagen...
        </p>
      </div>

      <div
        v-else
        class="space-y-2"
      >
        <UProgress
          :value="uploadProgress"
          color="primary"
        />
        <p class="text-sm text-neutral-300">
          Subiendo... {{ uploadProgress }}%
        </p>
      </div>
    </div>

    <!-- Mensaje de optimización -->
    <p
      v-if="optimizationInfo"
      class="text-sm text-success-600 dark:text-success-400"
    >
      {{ optimizationInfo }}
    </p>

    <!-- Mensaje de error -->
    <p
      v-if="error"
      class="text-sm text-error-500"
    >
      {{ error }}
    </p>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  modelValue?: string
  folder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'uploaded': [data: { key: string, url: string }]
}>()

const { uploadFile, isUploading, uploadProgress } = useS3Upload()
const { optimizeImage, formatFileSize } = useImageOptimizer()
const toast = useToast()

const fileInput = ref<HTMLInputElement>()
const previewUrl = ref<string>('')
const isDragging = ref(false)
const isOptimizing = ref(false)
const error = ref<string>('')
const optimizationInfo = ref<string>('')

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files[0]) {
    await processFile(input.files[0])
  }
}

const handleDrop = async (event: DragEvent) => {
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files && files[0]) {
    await processFile(files[0])
  }
}

const processFile = async (file: File) => {
  error.value = ''
  optimizationInfo.value = ''

  try {
    // Validar que sea una imagen
    if (!file.type.startsWith('image/')) {
      error.value = 'Por favor selecciona una imagen válida'
      return
    }

    // Optimizar imagen PRIMERO
    isOptimizing.value = true
    const { file: optimizedFile, originalSize, newSize, savings } = await optimizeImage(file)
    isOptimizing.value = false

    // Validar tamaño DESPUÉS de optimizar (10MB)
    const maxSize = 10 * 1024 * 1024
    if (optimizedFile.size > maxSize) {
      error.value = `La imagen sigue siendo muy grande después de optimizar (${formatFileSize(optimizedFile.size)}). Máximo 10MB.`
      return
    }

    // Mostrar información de optimización si hubo ahorro significativo
    if (savings > 5) {
      optimizationInfo.value = `✨ Imagen optimizada: ${formatFileSize(originalSize)} → ${formatFileSize(newSize)} (${savings.toFixed(0)}% más ligera)`

      toast.add({
        title: 'Imagen optimizada',
        description: `Reducida en ${savings.toFixed(0)}%: ${formatFileSize(originalSize)} → ${formatFileSize(newSize)}`,
        color: 'success'
      })
    }

    // Crear preview local con el archivo optimizado
    const reader = new FileReader()
    reader.onload = (e) => {
      previewUrl.value = e.target?.result as string
    }
    reader.readAsDataURL(optimizedFile)

    // Subir archivo optimizado a S3
    const result = await uploadFile(optimizedFile, props.folder || 'general')

    if (result) {
      emit('update:modelValue', result.url)
      emit('uploaded', { key: result.key, url: result.url })
      // Limpiar preview después de subir exitosamente
      previewUrl.value = ''
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    } else {
      error.value = 'Error al subir la imagen'
      previewUrl.value = ''
      optimizationInfo.value = ''
    }
  } catch (err) {
    console.error('Error processing file:', err)
    error.value = 'Error al procesar la imagen'
    isOptimizing.value = false
    optimizationInfo.value = ''
  }
}

const handleRemove = () => {
  emit('update:modelValue', '')
  previewUrl.value = ''
  error.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>
