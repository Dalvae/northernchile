<template>
  <UPopover
    v-model:open="showSuggestion"
    :ui="{ content: 'p-0' }"
    side="bottom"
    align="end"
  >
    <UDropdownMenu :items="items">
      <UButton
        color="neutral"
        variant="ghost"
        icon="i-heroicons-language"
        :aria-label="t('aria.select_language')"
        class="hover:bg-white/10"
      >
        <template
          v-if="showIndicator"
          #trailing
        >
          <span class="relative flex h-2 w-2">
            <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary-400 opacity-75" />
            <span class="relative inline-flex h-2 w-2 rounded-full bg-primary-500" />
          </span>
        </template>
      </UButton>
    </UDropdownMenu>

    <template #content>
      <div class="p-4 max-w-xs">
        <p class="text-sm text-neutral-700 dark:text-neutral-300 mb-3">
          {{ suggestionMessage }}
        </p>
        <div class="flex gap-2">
          <UButton
            size="sm"
            color="primary"
            @click="switchToSuggestedLocale"
          >
            {{ suggestionSwitch }}
          </UButton>
          <UButton
            size="sm"
            color="neutral"
            variant="ghost"
            @click="dismissSuggestion"
          >
            {{ t('language_suggestion.dismiss') }}
          </UButton>
        </div>
      </div>
    </template>
  </UPopover>
</template>

<script setup lang="ts">
const { locale, locales, setLocale, t } = useI18n()

const showSuggestion = ref(false)
const showIndicator = ref(false)
const suggestedLocale = ref<'es' | 'en' | 'pt' | null>(null)

// Map browser language prefixes to our locale codes
const languageMap: Record<string, string> = {
  es: 'es',
  en: 'en',
  pt: 'pt'
}

// Dynamic messages based on suggested locale
const suggestionMessage = computed(() => {
  if (suggestedLocale.value === 'en') return 'This site is available in English'
  if (suggestedLocale.value === 'pt') return 'Este site está disponível em Português'
  if (suggestedLocale.value === 'es') return 'Este sitio está disponible en Español'
  return ''
})

const suggestionSwitch = computed(() => {
  if (suggestedLocale.value === 'en') return 'Switch to English'
  if (suggestedLocale.value === 'pt') return 'Mudar para Português'
  if (suggestedLocale.value === 'es') return 'Cambiar a Español'
  return ''
})

const items = computed(() => {
  const menuItems = locales.value.map((l) => {
    return {
      label: l.name,
      onSelect: () => {
        setLocale(l.code)
        dismissSuggestion()
      }
    }
  })
  return [menuItems]
})

type SupportedLocale = 'es' | 'en' | 'pt'

function detectBrowserLanguage(): SupportedLocale | null {
  if (!import.meta.client) return null

  const browserLangs = navigator.languages || [navigator.language]

  for (const lang of browserLangs) {
    const prefix = lang.split('-')[0]?.toLowerCase()
    if (prefix && languageMap[prefix] && languageMap[prefix] !== locale.value) {
      return languageMap[prefix] as SupportedLocale
    }
  }

  return null
}

function switchToSuggestedLocale() {
  if (suggestedLocale.value) {
    setLocale(suggestedLocale.value as SupportedLocale)
  }
  dismissSuggestion()
}

function dismissSuggestion() {
  showSuggestion.value = false
  showIndicator.value = false
  // Save dismissal in cookie so we don't show again
  if (import.meta.client) {
    const dismissedCookie = useCookie('i18n_suggestion_dismissed', {
      maxAge: 60 * 60 * 24 * 30 // 30 days
    })
    dismissedCookie.value = 'true'
  }
}

onMounted(() => {
  // Check if user already dismissed
  const dismissedCookie = useCookie('i18n_suggestion_dismissed')
  if (dismissedCookie.value === 'true') return

  // Check if user already has the i18n redirect cookie (they chose a language)
  const redirectCookie = useCookie('i18n_redirected')
  if (redirectCookie.value) return

  // Detect browser language mismatch
  const detected = detectBrowserLanguage()
  if (detected) {
    suggestedLocale.value = detected
    showIndicator.value = true

    // Show popover after a short delay
    setTimeout(() => {
      showSuggestion.value = true
    }, 2000)
  }
})
</script>
