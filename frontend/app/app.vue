<script setup>
// Forzar dark mode en toda la aplicación
const colorMode = useColorMode()
colorMode.preference = 'dark'
</script>

<template>
  <UApp>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>

