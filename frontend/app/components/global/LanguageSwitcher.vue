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

<script setup>
const { locales } = useI18n()
const switchLocalePath = useSwitchLocalePath()

const items = computed(() => {
  const menuItems = locales.value.map(l => ({
    label: l.name,
    to: switchLocalePath(l.code)
  }))
  return [menuItems]
})
</script>
