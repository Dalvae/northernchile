<template>
  <UDropdownMenu :items="items">
    <UButton
      color="neutral"
      variant="ghost"
      icon="i-heroicons-language"
      :aria-label="t('aria.select_language')"
      class="hover:bg-white/10"
    />
  </UDropdownMenu>
</template>

<script setup lang="ts">
const { locale, locales, setLocale, t } = useI18n()

const items = computed(() => {
  const menuItems = locales.value.map((l) => {
    return {
      label: l.name,
      onSelect: () => setLocale(l.code)
    }
  })
  return [menuItems]
})
</script>
