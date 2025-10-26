<script setup lang="ts">
definePageMeta({
  layout: 'admin'
});
</script>

<template>
  <UPageHeader title="Gestión de Usuarios" description="Administra los usuarios del sistema." />
  <UPageBody>
    <UTable :rows="[]" :columns="[
      { id: 'id', key: 'id', label: 'ID' },
      { id: 'email', key: 'email', label: 'Email' },
      { id: 'fullName', key: 'fullName', label: 'Nombre Completo' },
      { id: 'role', key: 'role', label: 'Rol' },
      { id: 'actions', key: 'actions', label: 'Acciones' }
    ]" />
  </UPageBody>
</template>