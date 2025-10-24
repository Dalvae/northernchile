<template>
  <UHeader>
    <template #left>
      <NuxtLink to="/" class="flex items-center gap-2">
        <AppLogo class="w-auto h-8 text-primary-400" />
        <span class="font-display font-bold text-2xl text-gray-100">Northern Chile</span>
      </NuxtLink>
    </template>

    <template #center>
      <UButton
        v-for="link in links"
        :key="link.label"
        :to="link.to"
        variant="ghost"
        color="gray"
        class="font-semibold"
      >
        {{ link.label }}
      </UButton>
    </template>

    <template #right>
      <LanguageSwitcher />
      <UButton v-if="!authStore.isAuthenticated" :label="$t('nav.login')" color="gray" variant="ghost" to="/login" />
      <div v-else class="flex items-center gap-4">
        <UButton v-if="isAdmin" label="Admin" color="gray" variant="ghost" to="/admin" />
        <UButton label="Salir" color="gray" variant="ghost" @click="authStore.logout()" />
      </div>
    </template>
  </UHeader>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth';

const { t } = useI18n()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role?.includes('ADMIN'))

// Definimos los enlaces de navegación de forma reactiva
// para que se actualicen cuando cambie el idioma.
const links = computed(() => [
  {
    label: t('nav.tours'),
    to: '/'
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
