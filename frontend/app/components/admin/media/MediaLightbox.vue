<script setup lang="ts">
import type { MediaRes } from 'api-client'

const props = defineProps<{
  modelValue: boolean
  media: MediaRes[]
  initialIndex: number
}>()

const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentIndex = ref(props.initialIndex)

const currentMedia = computed(() => props.media[currentIndex.value])

function goToPrevious() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

function goToNext() {
  if (currentIndex.value < props.media.length - 1) {
    currentIndex.value++
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

function formatDate(dateString?: string) {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('es-CL')
}

function getTypeLabel(type?: string) {
  const labels: Record<string, string> = {
    TOUR: 'Tour',
    SCHEDULE: 'Salida',
    LOOSE: 'Suelto'
  }
  return labels[type || ''] || type || '-'
}

function getTypeBadgeColor(type?: string) {
  const colors: Record<string, string> = {
    TOUR: 'primary',
    SCHEDULE: 'secondary',
    LOOSE: 'neutral'
  }
  return colors[type || ''] || 'neutral'
}
</script>

<template>
  <UModal v-model:open="isOpen" :ui="{ width: 'sm:max-w-6xl' }">
    <template #content>
      <div class="relative bg-neutral-900 dark:bg-neutral-950">
        <!-- Close button -->
        <div class="absolute top-4 right-4 z-10">
          <UButton
            icon="i-heroicons-x-mark"
            color="neutral"
            variant="soft"
            @click="isOpen = false"
          />
        </div>

        <!-- Navigation arrows -->
        <div class="absolute inset-y-0 left-4 flex items-center z-10">
          <UButton
            v-if="currentIndex > 0"
            icon="i-heroicons-chevron-left"
            color="neutral"
            variant="soft"
            size="xl"
            @click="goToPrevious"
          />
        </div>

        <div class="absolute inset-y-0 right-4 flex items-center z-10">
          <UButton
            v-if="currentIndex < media.length - 1"
            icon="i-heroicons-chevron-right"
            color="neutral"
            variant="soft"
            size="xl"
            @click="goToNext"
          />
        </div>

        <!-- Image -->
        <div class="flex items-center justify-center min-h-[60vh] max-h-[80vh] p-16">
          <img
            v-if="currentMedia"
            :src="currentMedia.url"
            :alt="currentMedia.altTranslations?.es || currentMedia.originalFilename"
            class="max-w-full max-h-full object-contain"
          />
        </div>

        <!-- Info bar -->
        <div v-if="currentMedia" class="bg-neutral-800 dark:bg-neutral-900 p-6 border-t border-neutral-700 dark:border-neutral-800">
          <div class="flex items-start justify-between gap-4">
            <!-- Left side: Title and metadata -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 mb-2">
                <h3 class="text-lg font-semibold text-white truncate">
                  {{ currentMedia.altTranslations?.es || currentMedia.originalFilename }}
                </h3>
                <UBadge
                  :color="getTypeBadgeColor(currentMedia.type)"
                  variant="soft"
                >
                  {{ getTypeLabel(currentMedia.type) }}
                </UBadge>
              </div>

              <p v-if="currentMedia.captionTranslations?.es" class="text-sm text-neutral-300 mb-2">
                {{ currentMedia.captionTranslations.es }}
              </p>

              <div class="flex flex-wrap gap-2 mb-3">
                <UBadge
                  v-for="tag in currentMedia.tags"
                  :key="tag"
                  size="xs"
                  color="neutral"
                  variant="soft"
                >
                  {{ tag }}
                </UBadge>
              </div>

              <div class="flex gap-4 text-xs text-neutral-400">
                <span>{{ currentMedia.originalFilename }}</span>
                <span v-if="currentMedia.takenAt">{{ formatDate(currentMedia.takenAt) }}</span>
              </div>
            </div>

            <!-- Right side: Counter -->
            <div class="text-sm text-neutral-400">
              {{ currentIndex + 1 }} / {{ media.length }}
            </div>
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>
