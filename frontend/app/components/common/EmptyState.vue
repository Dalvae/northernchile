<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  title: string
  description?: string
  actionLabel?: string
  actionTo?: string
  actionIcon?: string
}>(), {
  icon: 'i-lucide-inbox',
  actionIcon: 'i-lucide-plus'
})

const emit = defineEmits<{
  (e: 'action'): void
}>()
</script>

<template>
  <div class="text-center py-12">
    <div
      class="w-20 h-20 bg-neutral-100 dark:bg-neutral-800 rounded-full flex items-center justify-center mx-auto mb-4"
    >
      <UIcon
        :name="icon"
        class="w-12 h-12 text-neutral-300 dark:text-neutral-600"
      />
    </div>

    <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-2">
      {{ title }}
    </h3>

    <p
      v-if="description"
      class="text-neutral-600 dark:text-neutral-300 mb-6 max-w-md mx-auto"
    >
      {{ description }}
    </p>

    <UButton
      v-if="actionLabel"
      color="primary"
      size="lg"
      :icon="actionIcon"
      :to="actionTo"
      @click="emit('action')"
    >
      {{ actionLabel }}
    </UButton>
  </div>
</template>
