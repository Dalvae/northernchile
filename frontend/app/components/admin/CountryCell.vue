<template>
  <div v-if="code" class="flex items-center gap-1.5">
    <span class="text-lg">{{ flag }}</span>
    <span class="text-sm">{{ label }}</span>
  </div>
  <span v-else>-</span>
</template>

<script setup lang="ts">
const props = defineProps<{ code?: string | null }>()

const { getCountryLabel, getCountryFlag } = useCountries()

const flag = computed(() =>
  props.code ? getCountryFlag(props.code) : ''
)

const label = computed(() =>
  props.code ? getCountryLabel(props.code) || props.code : ''
)
</script>
