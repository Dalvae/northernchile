<script setup lang="ts">
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  icon: {
    type: String,
    required: true,
  },
});
</script>

<template>
  <UCard>
    <div class="flex items-center gap-4">
      <UAvatar :icon="icon" size="lg" class="text-primary-500 dark:text-primary-400" />
      <div>
        <p class="text-gray-500 dark:text-gray-400 text-sm">{{ label }}</p>
        <p class="text-xl font-semibold">{{ value }}</p>
      </div>
    </div>
  </UCard>
</template>