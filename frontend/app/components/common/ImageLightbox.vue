<script setup lang="ts">
interface ImageItem {
  imageUrl: string
  alt?: string
}

const props = defineProps<{
  modelValue: boolean
  images: ImageItem[]
  initialIndex: number
}>()

const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentIndex = ref(props.initialIndex)

const currentImage = computed(() => props.images[currentIndex.value])

function goToPrevious() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  } else {
    // Loop to last image
    currentIndex.value = props.images.length - 1
  }
}

function goToNext() {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  } else {
    // Loop to first image
    currentIndex.value = 0
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (!isOpen.value) return

  if (event.key === 'ArrowLeft') {
    goToPrevious()
  } else if (event.key === 'ArrowRight') {
    goToNext()
  } else if (event.key === 'Escape') {
    isOpen.value = false
  }
}

// Watch for initial index changes
watch(() => props.initialIndex, (newIndex) => {
  currentIndex.value = newIndex
})

// Add keyboard listener
onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <UModal v-model:open="isOpen" :ui="{ width: 'sm:max-w-7xl' }">
    <template #content>
      <div class="relative bg-neutral-950">
        <!-- Close button -->
        <div class="absolute top-4 right-4 z-50">
          <UButton
            icon="i-heroicons-x-mark"
            color="neutral"
            variant="soft"
            size="lg"
            @click.stop="isOpen = false"
          />
        </div>

        <!-- Navigation arrows -->
        <div class="absolute inset-y-0 left-4 flex items-center z-10">
          <UButton
            icon="i-heroicons-chevron-left"
            color="neutral"
            variant="soft"
            size="xl"
            @click.stop="goToPrevious"
          />
        </div>

        <div class="absolute inset-y-0 right-4 flex items-center z-10">
          <UButton
            icon="i-heroicons-chevron-right"
            color="neutral"
            variant="soft"
            size="xl"
            @click.stop="goToNext"
          />
        </div>

        <!-- Image -->
        <div class="flex items-center justify-center min-h-[70vh] max-h-[85vh] p-20">
          <NuxtImg
            v-if="currentImage"
            :src="currentImage.imageUrl"
            :alt="currentImage.alt || `Image ${currentIndex + 1}`"
            class="max-w-full max-h-full object-contain rounded-lg"
            format="webp"
            loading="lazy"
            placeholder
          />
        </div>

        <!-- Counter bar -->
        <div class="bg-neutral-900/90 backdrop-blur-md p-4 border-t border-neutral-800">
          <div class="flex items-center justify-between">
            <div class="flex gap-2">
              <UButton
                icon="i-heroicons-chevron-left"
                color="neutral"
                variant="ghost"
                size="sm"
                :disabled="false"
                @click.stop="goToPrevious"
              >
                Anterior
              </UButton>
              <UButton
                icon="i-heroicons-chevron-right"
                color="neutral"
                variant="ghost"
                size="sm"
                trailing
                :disabled="false"
                @click.stop="goToNext"
              >
                Siguiente
              </UButton>
            </div>

            <div class="text-sm text-neutral-300 font-medium">
              {{ currentIndex + 1 }} / {{ images.length }}
            </div>

            <div class="text-xs text-neutral-500">
              Usa las flechas del teclado para navegar
            </div>
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>
