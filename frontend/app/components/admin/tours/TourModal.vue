<script setup lang="ts">
import type { TourRes } from "api-client";
import { useAdminTourForm } from "~/composables/useAdminTourForm";
import TourFormGeneral from "./form/General.vue";
import TourFormContent from './form/Content.vue'

const props = defineProps<{
  tour?: TourRes | null;
  open?: boolean;
}>();

const emit = defineEmits<{
  success: [];
  close: [];
  "update:open": [value: boolean];
}>();

const isEditing = computed(() => !!props.tour);

const { state, schema, loading, formErrors, onSubmit, onError } =
  useAdminTourForm(props, emit);

const isOpen = computed({
  get: () => props.open ?? false,
  set: (value) => emit("update:open", value),
});

const mainTab = ref(0);
const form = ref();

const items = computed(() => [
  {
    key: "general",
    label: "Información General",
    icon: "i-heroicons-information-circle",
    slot: "general",
  },
  {
    key: "gallery",
    label: "Galería",
    icon: "i-heroicons-photo",
    disabled: !isEditing.value,
    slot: "gallery",
  },
]);

const handleSubmit = () => {
  form.value?.submit();
};
</script>

<template>
  <UModal
    v-model:open="isOpen"
    :title="isEditing ? 'Editar Tour' : 'Crear Nuevo Tour'"
    :ui="{
      content: 'max-w-5xl',
      body: 'max-h-[70vh] overflow-y-auto'
    }"
  >
    <template #body>
      <UTabs
        v-model="mainTab"
        :items="items"
        class="w-full"
      >
        <!-- Slot: GENERAL -->
        <template #general>
          <div class="py-4">
            <UForm
              ref="form"
              :schema="schema"
              :state="state"
              @submit="onSubmit"
              @error="onError"
              class="space-y-8"
            >
              <TourFormGeneral :state="state" :errors="formErrors" />
              <UDivider />
              <TourFormContent :state="state" />
            </UForm>
          </div>
        </template>

        <!-- Slot: GALLERY -->
        <template #gallery>
          <div class="py-4">
            <AdminMediaGalleryManager
              v-if="tour?.id"
              :tour-id="tour.id"
            />
          </div>
        </template>
      </UTabs>
    </template>

    <template #footer>
      <div class="flex justify-between items-center w-full">
        <span class="text-sm text-muted">
          {{ mainTab === 0 ? 'Revisa los campos antes de guardar' : 'Gestión de imágenes' }}
        </span>
        <div class="flex gap-3">
          <UButton label="Cancelar" color="neutral" variant="ghost" @click="emit('close')" />
          <UButton
            v-if="mainTab === 0"
            :label="isEditing ? 'Guardar Cambios' : 'Crear Tour'"
            :loading="loading"
            color="primary"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>