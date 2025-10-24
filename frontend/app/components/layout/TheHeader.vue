<template>
  <UHeader :links="links">
    <template #left>
      <NuxtLink to="/" class="flex items-center gap-2">
        <AppLogo class="w-auto h-8" />
        <span class="font-bold text-xl">Northern Chile</span>
      </NuxtLink>
    </template>

    <template #right>
      <LanguageSwitcher />
      <UButton :label="$t('nav.login')" color="gray" variant="ghost" />
      <UColorModeButton />
    </template>
  </UHeader>
</template>

<script setup lang="ts">
const { t } = useI18n()

// Definimos los enlaces de navegación de forma reactiva
// para que se actualicen cuando cambie el idioma.
const links = computed(() => [
  {
    label: t('nav.tours'),
    to: '/tours'
  },
  {
    label: t('nav.private_tours'),
    to: '/private-tours'
  },
  {
    label: t('nav.about_us'),
    to: '/about'
  }
])
</script>
