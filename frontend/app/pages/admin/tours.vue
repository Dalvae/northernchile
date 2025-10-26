<script setup lang="ts">
definePageMeta({
  layout: 'admin'
});
</script>

<template>
  <UPageHeader title="Gestión de Tours" description="Administra los tours disponibles." />
  <UPageBody>
    <div class="flex justify-end mb-4">
      <UButton icon="i-lucide-plus" label="Crear Nuevo Tour" />
    </div>
    <UTable :rows="[]" :columns="[
      { id: 'name', key: 'name', label: 'Nombre' },
      { id: 'category', key: 'category', label: 'Categoría' },
      { id: 'owner', key: 'owner', label: 'Propietario' },
      { id: 'status', key: 'status', label: 'Estado' },
      { id: 'actions', key: 'actions', label: 'Acciones' }
    ]" />
  </UPageBody>
</template>