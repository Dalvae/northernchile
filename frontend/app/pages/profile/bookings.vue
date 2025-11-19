<script setup lang="ts">
definePageMeta({
  layout: 'default'
})
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-12">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          Mis Reservas
        </h1>
        <p class="text-neutral-600 dark:text-neutral-300">
          Revisa y administra tus reservas
        </p>
      </div>

      <!-- Bookings Component -->
      <ProfileBookingsList />
    </UContainer>
  </div>
</template>
