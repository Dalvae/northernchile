<script setup lang="ts">
definePageMeta({
  layout: 'admin'
});
</script>

<template>
  <UPageHeader title="Gestión de Reservas" description="Administra todas las reservas del sistema." />
  <UPageBody>
    <div class="flex justify-between items-center mb-4">
      <UInput placeholder="Buscar por cliente..." class="w-1/3" />
      <USelectMenu :options="['Todas', 'Confirmadas', 'Canceladas']" placeholder="Filtrar por estado" />
    </div>
    <UTable :rows="[]" :columns="[
      { id: 'id', key: 'id', label: 'ID Reserva' },
      { id: 'tourDate', key: 'tourDate', label: 'Fecha Tour' },
      { id: 'tour', key: 'tour', label: 'Tour' },
      { id: 'client', key: 'client', label: 'Cliente' },
      { id: 'passengers', key: 'passengers', label: 'N° Pasajeros' },
      { id: 'total', key: 'total', label: 'Total' },
      { id: 'status', key: 'status', label: 'Estado' }
    ]" />
  </UPageBody>
</template>