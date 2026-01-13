<template>
  <div
    v-if="showBanner"
    class="bg-warning/90 text-warning-950 py-2 px-4 text-center text-sm font-medium sticky top-0 z-50"
  >
    <div class="flex items-center justify-center gap-2">
      <UIcon
        name="i-lucide-triangle-alert"
        class="size-4"
      />
      <span>{{ t('dev_warning.banner') }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const config = useRuntimeConfig()

// Show banner only if payment test mode is enabled
const showBanner = computed(() => config.public.paymentTestMode)
</script>
