<template>
  <UDropdownMenu :items="items">
    <UButton
      color="neutral"
      variant="ghost"
      icon="i-heroicons-language"
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
