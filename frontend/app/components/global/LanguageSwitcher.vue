<template>
  <UDropdownMenu :items="items">
    <UButton
      color="white"
      variant="ghost"
      icon="i-heroicons-language"
      aria-label="Seleccionar idioma"
      class="hover:bg-white/10"
    />
  </UDropdownMenu>
</template>

<script setup lang="ts">
const { locales, setLocale } = useI18n()
const switchLocalePath = useSwitchLocalePath()

const items = computed(() => {
  const menuItems = locales.value.map((l) => {
    const path = switchLocalePath(l.code)
    if (path && typeof path === 'string' && !path.includes('undefined')) {
      return {
        label: l.name,
        to: path
      }
    }
    return {
      label: l.name,
      onClick: () => setLocale(l.code)
    }
  })
  return [menuItems]
})
</script>
