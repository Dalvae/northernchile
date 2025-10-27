<template>
  <UHeader>
    <template #left>
      <NuxtLink to="/" class="flex items-center gap-2">
        <span class="font-display font-bold text-2xl text-gray-900 dark:text-gray-100">
          Northern Chile
        </span>
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

      <UButton
        v-if="!authStore.isAuthenticated"
        label="Iniciar Sesión"
        color="gray"
        variant="ghost"
        to="/auth"
      />

      <div v-else class="flex items-center gap-4">
        <UButton
          v-if="isAdmin"
          label="Admin"
          color="gray"
          variant="ghost"
          to="/admin"
        />
        <UButton label="Mi Cuenta" color="gray" variant="ghost" to="/account" />
        <UButton
          label="Salir"
          color="gray"
          variant="ghost"
          @click="authStore.logout()"
        />
      </div>
    </template>
  </UHeader>
</template>

<script setup lang="ts">
import { useAuthStore } from "~/stores/auth";

const { t } = useI18n();
const authStore = useAuthStore();
const localePath = useLocalePath();

const isAdmin = computed(() => authStore.isAdmin);

const links = computed(() => [
  { 
    label: t("nav.tours"),
    to: localePath("/"),
  },
  {
    label: t("nav.private_tours"),
    to: localePath("/private-tours"),
  },
  {
    label: t("nav.about_us"),
    to: localePath("/about"),
  },
]);
</script>