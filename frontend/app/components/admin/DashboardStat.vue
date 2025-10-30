<script setup lang="ts">
interface Props {
  label: string
  value: string | number
  icon: string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  color: 'primary'
})
</script>
<template>
  <UCard>
    <div class="flex items-center gap-4">
      <div class="flex-shrink-0">
         <UIcon :name="icon" class="w-8 h-8" :class="`text-${color}-500 dark:text-${color}-400`" />
      </div>
      <div>
        <p class="text-sm font-medium text-neutral-500 dark:text-neutral-400">
          {{ label }}
        </p>
        <p class="text-2xl font-semibold text-neutral-900 dark:text-white">
          {{ value }}
        </p>
      </div>
    </div>
  </UCard>
</template>