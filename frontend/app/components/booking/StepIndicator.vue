<template>
  <div class="flex items-center">
    <!-- Step Circle -->
    <div class="flex items-center justify-center">
      <div
        class="w-10 h-10 rounded-full flex items-center justify-center transition-colors"
        :class="[
          completed ? 'bg-success text-white' : active ? 'bg-primary text-white' : 'bg-neutral-200 dark:bg-neutral-700 text-neutral-600 dark:text-neutral-400'
        ]"
      >
        <UIcon v-if="completed" name="i-lucide-check" class="w-5 h-5" />
        <span v-else class="font-semibold">{{ step }}</span>
      </div>
    </div>

    <!-- Step Label -->
    <div class="ml-3 min-w-0 hidden sm:block">
      <p
        class="text-sm font-medium transition-colors"
        :class="[
          active || completed ? 'text-neutral-900 dark:text-white' : 'text-neutral-500 dark:text-neutral-400'
        ]"
      >
        {{ title }}
      </p>
    </div>

    <!-- Connector Line -->
    <div
      v-if="!isLast"
      class="flex-1 mx-4 h-0.5 transition-colors"
      :class="[
        completed ? 'bg-success' : 'bg-neutral-200 dark:bg-neutral-700'
      ]"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  step: number
  title: string
  active: boolean
  completed: boolean
  isLast: boolean
}>()
</script>
