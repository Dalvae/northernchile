<script setup lang="ts">
const { t, tm } = useI18n()

useSeoMeta({
  title: t('legal.cancellation.title'),
  description: t('legal.cancellation.intro').substring(0, 160)
})

const sections = computed(() => {
  const sectionsArray = tm('legal.cancellation.sections') as unknown[]
  return Array.isArray(sectionsArray) ? sectionsArray.length : 0
})
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-950 py-16">
    <UContainer class="max-w-4xl">
      <div class="mb-12 text-center">
        <div class="w-16 h-16 bg-warning/10 rounded-full flex items-center justify-center mx-auto mb-6">
          <UIcon name="i-lucide-rotate-ccw" class="w-8 h-8 text-warning" />
        </div>
        <h1 class="text-3xl md:text-4xl font-bold text-neutral-900 dark:text-white mb-4">
          {{ t('legal.cancellation.title') }}
        </h1>
        <p class="text-lg text-neutral-600 dark:text-neutral-400">
          {{ t('legal.cancellation.intro') }}
        </p>
      </div>

      <!-- Resumen visual de la regla de oro -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">
        <div class="bg-success/10 border border-success/20 rounded-xl p-6">
          <div class="flex items-start gap-4">
            <UIcon name="i-lucide-check-circle" class="w-6 h-6 text-success mt-1" />
            <div>
              <h3 class="font-bold text-neutral-900 dark:text-white mb-1">
                {{ t('legal.cancellation.summary.more_than_24h.title') }}
              </h3>
              <p class="text-sm text-neutral-700 dark:text-neutral-300">
                {{ t('legal.cancellation.summary.more_than_24h.description') }}
              </p>
            </div>
          </div>
        </div>

        <div class="bg-error/10 border border-error/20 rounded-xl p-6">
          <div class="flex items-start gap-4">
            <UIcon name="i-lucide-alert-circle" class="w-6 h-6 text-error mt-1" />
            <div>
              <h3 class="font-bold text-neutral-900 dark:text-white mb-1">
                {{ t('legal.cancellation.summary.less_than_24h.title') }}
              </h3>
              <p class="text-sm text-neutral-700 dark:text-neutral-300">
                {{ t('legal.cancellation.summary.less_than_24h.description') }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <UCard class="prose dark:prose-invert max-w-none p-6 sm:p-10">
        <div class="space-y-8">
          <div v-for="index in sections" :key="index">
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-3">
              {{ t(`legal.cancellation.sections.${index - 1}.title`) }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-300 leading-relaxed">
              {{ t(`legal.cancellation.sections.${index - 1}.content`) }}
            </p>
          </div>
        </div>
      </UCard>
    </UContainer>
  </div>
</template>
