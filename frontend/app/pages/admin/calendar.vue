<script setup lang="ts">
definePageMeta({
  layout: 'admin'
});
</script>

<template>
  <UPageHeader title="Calendario y Agenda" description="Gestiona la disponibilidad de los tours." />
  <UPageBody>
    <!-- Aquí irá el componente v-calendar -->
    <UCard>
      <template #header>
        <h3>Calendario</h3>
      </template>
      <p>Componente de calendario (v-calendar) aquí.</p>
    </UCard>
  </UPageBody>
</template>