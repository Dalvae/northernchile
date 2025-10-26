<script setup lang="ts">
// ESTA LÍNEA ES LA CLAVE: Le dice a la página que use el layout 'admin'
definePageMeta({
  layout: 'admin'
});
</script>

<template>
  <div>
    <UPageHeader
      title="Dashboard"
      description="Vista general del sistema de administración."
    />

    <UPageBody>
      <div class="space-y-4">
        <UCard>
          <template #header>
            Reservas Hoy
          </template>
          <!-- Aquí irán los datos reales -->
          0
        </UCard>

        <UCard>
          <template #header>
            Ingresos del Mes
          </template>
          <!-- Aquí irán los datos reales -->
          $0.00
        </UCard>

        <UCard>
          <template #header>
            Próximos Tours
          </template>
          <!-- Aquí irá una UTable con los próximos tours -->
          <div class="text-center text-gray-500">No data</div>
        </UCard>
      </div>
    </UPageBody>
  </div>
</template>