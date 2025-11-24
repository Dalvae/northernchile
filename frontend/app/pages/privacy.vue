<script setup lang="ts">
const { t, tm } = useI18n()

useSeoMeta({
  title: t('legal.privacy.title'),
  description: t('legal.privacy.intro').substring(0, 160),
  robots: 'noindex, follow'
})

const sections = computed(() => {
  const sectionsArray = tm('legal.privacy.sections')
  return Array.isArray(sectionsArray) ? sectionsArray.length : 0
})
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-950 py-16">
    <UContainer class="max-w-4xl">
      <div class="mb-8 text-center">
        <div class="w-16 h-16 bg-primary/10 rounded-full flex items-center justify-center mx-auto mb-6">
          <UIcon name="i-lucide-shield-check" class="w-8 h-8 text-primary" />
        </div>
        <h1 class="text-3xl md:text-4xl font-bold text-neutral-900 dark:text-white mb-4">
          {{ t('legal.privacy.title') }}
        </h1>
      </div>

      <UCard class="prose dark:prose-invert max-w-none p-6 sm:p-10">
        <p class="lead text-lg text-neutral-700 dark:text-neutral-300 mb-8">
          {{ t('legal.privacy.intro') }}
        </p>

        <div class="space-y-8">
          <div v-for="index in sections" :key="index">
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-3">
              {{ t(`legal.privacy.sections.${index - 1}.title`) }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-300 leading-relaxed">
              {{ t(`legal.privacy.sections.${index - 1}.content`) }}
            </p>
          </div>
        </div>
      </UCard>
    </UContainer>
  </div>
</template>
