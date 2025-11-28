<template>
  <UDropdownMenu :items="items">
    <UButton
      color="neutral"
      variant="ghost"
      icon="i-heroicons-language"
      aria-label="Seleccionar idioma"
      class="hover:bg-white/10"
    />
  </UDropdownMenu>
</template>

<script setup lang="ts">
const { locales } = useI18n()
const switchLocalePath = useSwitchLocalePath()

const items = computed(() => {
  const menuItems = locales.value.map((l) => {
    return {
      label: l.name,
      to: switchLocalePath(l.code) || '/'
    }
  })
  return [menuItems]
})
</script>
