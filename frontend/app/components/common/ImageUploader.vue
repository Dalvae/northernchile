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
        v-if="!isUploading"
        class="space-y-2"
      >
        <div class="flex justify-center">
          <UIcon
            name="i-heroicons-cloud-arrow-up"
            class="w-12 h-12 text-neutral-500"
          />
        </div>
        <p class="text-neutral-400">
          {{ isDragging ? 'Suelta la imagen aquí' : 'Arrastra una imagen o haz clic para seleccionar' }}
        </p>
        <p class="text-sm text-neutral-500">
          PNG, JPG, GIF hasta 5MB
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
        <p class="text-sm text-neutral-400">
          Subiendo... {{ uploadProgress }}%
        </p>
      </div>
    </div>

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

const fileInput = ref<HTMLInputElement>()
const previewUrl = ref<string>('')
const isDragging = ref(false)
const error = ref<string>('')

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

  // Crear preview local
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  // Subir a S3
  const result = await uploadFile(file, props.folder || 'general')

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
