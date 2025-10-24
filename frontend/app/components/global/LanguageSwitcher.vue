<template>
  <UDropdownMenu :items="availableLocales">
    <UButton color="gray" variant="ghost" icon="i-heroicons-language" />
  </UDropdownMenu>
</template>

<script setup>
const { locale, locales, setLocale } = useI18n()
const switchLocalePath = useSwitchLocalePath()
console.log('Locales in LanguageSwitcher:', locales.value)

const availableLocales = computed(() => {
  return locales.value.map(l => ({
    label: l.language,
    click: () => {
      console.log('Current locale before change:', locale.value)
      setLocale(l.code) // Update the locale
      console.log('Locale after setLocale:', locale.value)
      navigateTo(switchLocalePath(l.code)) // Navigate to the localized path
    }
  }))
})
</script>
