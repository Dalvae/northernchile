<script setup lang="ts">
const { t, tm } = useI18n()

useSeoMeta({
  title: t('legal.terms.title'),
  description: t('legal.terms.intro').substring(0, 160),
  robots: 'noindex, follow'
})

// Obtenemos el número de secciones
const sections = computed(() => {
  const sectionsArray = tm('legal.terms.sections') as unknown[]
  return Array.isArray(sectionsArray) ? sectionsArray.length : 0
})
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-950 py-16">
    <UContainer class="max-w-4xl">
      <div class="mb-8 text-center">
        <h1 class="text-3xl md:text-4xl font-bold text-neutral-900 dark:text-white mb-4">
          {{ t('legal.terms.title') }}
        </h1>
        <p class="text-sm text-neutral-500 dark:text-neutral-400">
          {{ t('legal.terms.last_updated') }}
        </p>
      </div>

      <UCard class="prose dark:prose-invert max-w-none p-6 sm:p-10">
        <p class="lead text-lg text-neutral-700 dark:text-neutral-300 mb-8">
          {{ t('legal.terms.intro') }}
        </p>

        <div class="space-y-8">
          <div v-for="index in sections" :key="index">
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white mb-3">
              {{ t(`legal.terms.sections.${index - 1}.title`) }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-300 leading-relaxed">
              {{ t(`legal.terms.sections.${index - 1}.content`) }}
            </p>
          </div>
        </div>

        <div class="mt-12 pt-8 border-t border-neutral-200 dark:border-neutral-800 text-sm text-neutral-500">
          <p>Northern Chile Tours | San Pedro de Atacama, Chile.</p>
        </div>
      </UCard>
    </UContainer>
  </div>
</template>
