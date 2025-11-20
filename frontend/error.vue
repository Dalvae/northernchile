<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{
  error: NuxtError
}>()

const { t } = useI18n()

const errorMessages = computed(() => {
  const status = props.error.statusCode || 500

  switch (status) {
    case 404:
      return {
        title: t('error.404.title'),
        message: t('error.404.message'),
        icon: 'i-heroicons-map-20-solid'
      }
    case 403:
      return {
        title: t('error.403.title'),
        message: t('error.403.message'),
        icon: 'i-heroicons-lock-closed-20-solid'
      }
    case 500:
    default:
      return {
        title: t('error.500.title'),
        message: t('error.500.message'),
        icon: 'i-heroicons-exclamation-triangle-20-solid'
      }
  }
})

const handleClearError = () => {
  clearError({ redirect: '/' })
}

const handleGoBack = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    handleClearError()
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-neutral-50 dark:bg-neutral-950 px-4">
    <div class="max-w-2xl w-full text-center">
      <!-- Error Icon -->
      <div class="flex justify-center mb-8">
        <div class="w-24 h-24 rounded-full bg-error-100 dark:bg-error-900/20 flex items-center justify-center">
          <UIcon
            :name="errorMessages.icon"
            class="w-12 h-12 text-error-600 dark:text-error-400"
          />
        </div>
      </div>

      <!-- Error Code -->
      <div class="mb-4">
        <span class="inline-block px-4 py-2 rounded-full bg-neutral-100 dark:bg-neutral-800 text-neutral-600 dark:text-neutral-400 text-sm font-medium">
          {{ t('error.code') }}: {{ error.statusCode || 500 }}
        </span>
      </div>

      <!-- Error Title -->
      <h1 class="text-4xl md:text-5xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
        {{ errorMessages.title }}
      </h1>

      <!-- Error Message -->
      <p class="text-lg text-neutral-600 dark:text-neutral-400 mb-8 max-w-md mx-auto">
        {{ errorMessages.message }}
      </p>

      <!-- Desert/Space themed illustration text -->
      <div class="mb-12 text-6xl">
        🌌🏜️
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
        <UButton
          size="lg"
          color="primary"
          icon="i-heroicons-home-20-solid"
          @click="handleClearError"
        >
          {{ t('error.goHome') }}
        </UButton>

        <UButton
          size="lg"
          color="secondary"
          variant="outline"
          icon="i-heroicons-arrow-left-20-solid"
          @click="handleGoBack"
        >
          {{ t('error.goBack') }}
        </UButton>
      </div>

      <!-- Help Text -->
      <div class="mt-12 pt-8 border-t border-neutral-200 dark:border-neutral-800">
        <p class="text-sm text-neutral-500 dark:text-neutral-500">
          {{ t('error.needHelp') }}
          <NuxtLink
            to="/contact"
            class="text-primary-600 dark:text-primary-400 hover:underline font-medium"
          >
            {{ t('error.contactUs') }}
          </NuxtLink>
        </p>
      </div>

      <!-- Debug info (only in development) -->
      <div v-if="$config.public.dev" class="mt-8 p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg text-left text-xs">
        <p class="font-mono text-neutral-600 dark:text-neutral-400 mb-2">
          <strong>{{ t('error.debugInfo') }}:</strong>
        </p>
        <pre class="text-neutral-600 dark:text-neutral-400 whitespace-pre-wrap break-words">{{ error }}</pre>
      </div>
    </div>
  </div>
</template>
