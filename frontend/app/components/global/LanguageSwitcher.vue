<template>
  <UDropdownMenu :items="items">
    <UButton color="gray" variant="ghost" icon="i-heroicons-language" />
  </UDropdownMenu>
</template>

<script setup>
const { locales, setLocale } = useI18n()

const items = computed(() => {
  const menuItems = locales.value.map(l => ({
    label: l.name,
    // La función setLocale se encargará de la navegación a la URL correcta.
    click: () => setLocale(l.code)
  }))
  // UDropdownMenu espera un array de arrays para los grupos de items.
  return [menuItems]
})
</script>
