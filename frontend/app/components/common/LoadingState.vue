<script setup lang="ts">
withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg' | 'xl'
  text?: string
}>(), {
  size: 'md'
})

const sizeClasses = {
  sm: 'w-5 h-5',
  md: 'w-8 h-8',
  lg: 'w-12 h-12',
  xl: 'w-16 h-16'
}

const paddingClasses = {
  sm: 'py-6',
  md: 'py-12',
  lg: 'py-16',
  xl: 'py-20'
}
</script>

<template>
  <div
    class="flex flex-col items-center justify-center"
    :class="paddingClasses[size]"
  >
    <UIcon
      name="i-lucide-loader-2"
      class="animate-spin text-primary"
      :class="sizeClasses[size]"
    />
    <p
      v-if="text"
      class="mt-3 text-sm text-neutral-600 dark:text-neutral-300"
    >
      {{ text }}
    </p>
  </div>
</template>
