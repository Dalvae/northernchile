<script setup lang="ts">
const props = defineProps<{
  label: string
  value: number | string
  icon: string
  change?: number
  iconColor?: string
  iconTextColor?: string
  loading?: boolean
  formatter?: (value: number | string) => string
}>()

const formattedValue = computed(() => {
  if (props.formatter) {
    return props.formatter(props.value)
  }
  return props.value.toString()
})

const changeColor = computed(() => {
  if (props.change === undefined) return ''
  return props.change >= 0 ? 'text-success' : 'text-error'
})

const changeIcon = computed(() => {
  if (props.change === undefined) return ''
  return props.change >= 0 ? 'i-lucide-trending-up' : 'i-lucide-trending-down'
})
</script>

<template>
  <UCard>
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <p class="text-sm font-medium text-neutral-600 dark:text-neutral-400">
          {{ label }}
        </p>

        <!-- Loading state -->
        <USkeleton
          v-if="loading"
          class="h-8 w-24 mt-2"
        />

        <!-- Value -->
        <p
          v-else
          class="mt-2 text-3xl font-semibold text-neutral-900 dark:text-white"
        >
          {{ formattedValue }}
        </p>

        <!-- Cambio vs período anterior -->
        <div
          v-if="change !== undefined && !loading"
          class="mt-2 flex items-center gap-1"
        >
          <UIcon
            :name="changeIcon"
            :class="changeColor"
            class="w-4 h-4"
          />
          <span
            :class="changeColor"
            class="text-sm font-medium"
          >
            {{ Math.abs(change) }}%
          </span>
          <span class="text-xs text-neutral-500">vs período anterior</span>
        </div>
      </div>

      <!-- Icon -->
      <div
        :class="[
          'p-3 rounded-lg',
          iconColor || 'bg-primary/10'
        ]"
      >
        <UIcon
          :name="icon"
          :class="iconTextColor || 'text-primary'"
          class="w-6 h-6"
        />
      </div>
    </div>
  </UCard>
</template>
