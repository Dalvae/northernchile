<script setup lang="ts">
import type { MediaRes } from 'api-client'
import type { BadgeColor } from '~/types/ui'

const props = defineProps<{
  modelValue: boolean
  media: MediaRes[]
  initialIndex: number
  totalItems?: number
  hasMore?: boolean
  loadingMore?: boolean
}>()

const emit = defineEmits(['update:modelValue', 'load-more'])

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const { currentIndex, currentItem, goToPrevious, goToNext, canGoPrevious } = useLightboxNavigation(
  toRef(() => props.media),
  toRef(() => props.initialIndex),
  {
    loop: false,
    onClose: () => { isOpen.value = false },
    onReachEnd: () => {
      if (props.hasMore && !props.loadingMore) {
        emit('load-more')
      }
    }
  }
)

const currentMedia = computed(() => currentItem.value as MediaRes | undefined)

// When more media is loaded, advance to next item
watch(() => props.media.length, (newLength, oldLength) => {
  if (currentIndex.value === oldLength - 1 && newLength > oldLength) {
    goToNext()
  }
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

function getTypeBadgeColor(type?: string): BadgeColor {
  const colors: Record<string, BadgeColor> = {
    TOUR: 'primary',
    SCHEDULE: 'secondary',
    LOOSE: 'neutral'
  }
  return colors[type || ''] || 'neutral'
}
</script>

<template>
  <UModal
    v-model:open="isOpen"
    class="sm:max-w-6xl"
  >
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
            v-if="canGoPrevious"
            icon="i-heroicons-chevron-left"
            color="neutral"
            variant="soft"
            size="xl"
            @click="goToPrevious"
          />
        </div>

        <div class="absolute inset-y-0 right-4 flex items-center z-10">
          <UButton
            v-if="currentIndex < media.length - 1 || hasMore"
            :icon="loadingMore ? 'i-heroicons-arrow-path' : 'i-heroicons-chevron-right'"
            :class="{ 'animate-spin': loadingMore }"
            color="neutral"
            variant="soft"
            size="xl"
            :disabled="loadingMore"
            @click="goToNext"
          />
        </div>

        <!-- Image -->
        <div class="flex items-center justify-center min-h-[60vh] max-h-[80vh] p-16">
          <NuxtImg
            v-if="currentMedia"
            :src="currentMedia.url"
            :alt="currentMedia.altTranslations?.es || currentMedia.originalFilename"
            class="max-w-full max-h-full object-contain"
            format="webp"
            loading="lazy"
            placeholder
          />
        </div>

        <!-- Info bar -->
        <div
          v-if="currentMedia"
          class="bg-neutral-800 dark:bg-neutral-900 p-6 border-t border-neutral-700 dark:border-neutral-800"
        >
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

              <p
                v-if="currentMedia.captionTranslations?.es"
                class="text-sm text-neutral-200 mb-2"
              >
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

              <div class="flex gap-4 text-xs text-neutral-300">
                <span>{{ currentMedia.originalFilename }}</span>
                <span v-if="currentMedia.takenAt">{{ formatDate(currentMedia.takenAt) }}</span>
              </div>
            </div>

            <!-- Right side: Counter -->
            <div class="text-sm text-neutral-300">
              {{ currentIndex + 1 }} / {{ totalItems || media.length }}
            </div>
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>
