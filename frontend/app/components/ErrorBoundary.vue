<template>
  <div
    v-if="error"
    class="min-h-screen flex items-center justify-center bg-neutral-100 dark:bg-neutral-900"
  >
    <div class="text-center p-8 max-w-md">
      <div class="text-6xl mb-4">
        {{ getErrorEmoji() }}
      </div>
      <h1 class="text-2xl font-bold text-default mb-2">
        {{ $t('error.unexpected') }}
      </h1>
      <p class="text-muted mb-6">
        {{ $t('error.tryAgain') }}
      </p>
      <div class="flex gap-3 justify-center">
        <UButton
          color="neutral"
          variant="outline"
          @click="goBack"
        >
          {{ $t('error.goBack') }}
        </UButton>
        <UButton
          color="primary"
          @click="reload"
        >
          {{ $t('error.reload') }}
        </UButton>
      </div>
      <p
        v-if="showDetails && error.message"
        class="mt-6 text-xs text-muted font-mono bg-neutral-200 dark:bg-neutral-800 p-3 rounded"
      >
        {{ error.message }}
      </p>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
const props = defineProps<{
  showDetails?: boolean
}>()

const error = ref<Error | null>(null)
const router = useRouter()

onErrorCaptured((err, instance, info) => {
  console.error('Error boundary caught:', err)
  console.error('Component:', instance)
  console.error('Info:', info)
  error.value = err
  return false // Prevent error from propagating
})

function getErrorEmoji() {
  // Random selection for variety
  const emojis = ['😕', '🙁', '😔', '🤔']
  return emojis[Math.floor(Math.random() * emojis.length)]
}

function reload() {
  error.value = null
  window.location.reload()
}

function goBack() {
  error.value = null
  router.back()
}

// Reset error when navigating
watch(() => router.currentRoute.value, () => {
  error.value = null
})
</script>
