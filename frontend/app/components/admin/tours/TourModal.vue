<script setup lang="ts">
import { z } from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";
import type { TourRes, TourCreateReq, TourUpdateReq } from "~/lib/api-client";

const props = defineProps<{
  tour?: TourRes | null;
}>();

const emit = defineEmits<{
  success: [];
}>();

const isEditing = computed(() => !!props.tour);

// Schema con mensajes de error en español
const schema = z.object({
  name: z.string().min(3, "El nombre debe tener al menos 3 caracteres"),
  description: z
    .string()
    .min(10, "La descripción debe tener al menos 10 caracteres"),
  category: z.string().min(1, "La categoría es requerida"),
  priceAdult: z.number().min(1, "El precio debe ser mayor a 0"),
  priceChild: z.number().min(0, "El precio no puede ser negativo").nullable(),
  defaultMaxParticipants: z
    .number()
    .int()
    .min(1, "Debe ser al menos 1 participante"),
  durationHours: z.number().int().min(1, "Debe ser al menos 1 hora"),
  status: z.enum(["DRAFT", "PUBLISHED", "ARCHIVED"]),
});

type Schema = z.output<typeof schema>;

// Estado inicial con tipos correctos
const initialState: Schema = {
  name: "",
  description: "",
  category: "ASTRONOMICAL",
  priceAdult: 0,
  priceChild: null,
  defaultMaxParticipants: 10,
  durationHours: 2,
  status: "DRAFT",
};

const state = reactive<Schema>({ ...initialState });
const form = ref();

// Configurar estado basado en props.tour
watch(
  () => props.tour,
  (tour) => {
    if (tour) {
      state.name = tour.name;
      state.description = tour.description;
      state.category = tour.category;
      state.priceAdult = tour.priceAdult;
      state.priceChild = tour.priceChild;
      state.defaultMaxParticipants = tour.defaultMaxParticipants;
      state.durationHours = tour.durationHours;
      state.status = tour.status;
    } else {
      // Resetear a valores por defecto
      Object.assign(state, initialState);
    }
  },
  { immediate: true, deep: true },
);

// Submit function
const { createAdminTour, updateAdminTour } = useAdminData();
const toast = useToast();
const loading = ref(false);

async function onSubmit(event: FormSubmitEvent<Schema>) {
  console.log("Form submitted with data:", event.data);
  loading.value = true;
  try {
    const data = event.data;

    if (isEditing.value && props.tour?.id) {
      await updateAdminTour(props.tour.id, data as TourUpdateReq);
      toast.add({
        title: "Tour actualizado con éxito",
        color: "green",
        icon: "i-heroicons-check-circle",
      });
    } else {
      await createAdminTour(data as TourCreateReq);
      toast.add({
        title: "Tour creado con éxito",
        color: "green",
        icon: "i-heroicons-check-circle",
      });
    }
    emit("success");
  } catch (error: any) {
    console.error("Error submitting form:", error);
    toast.add({
      title: "Error",
      description: error.message || "No se pudo guardar el tour",
      color: "red",
      icon: "i-heroicons-exclamation-triangle",
    });
  } finally {
    loading.value = false;
  }
}

// Opciones para los selects
const categoryOptions = [
  { value: "ASTRONOMICAL", label: "Astronómico" },
  { value: "REGULAR", label: "Regular" },
  { value: "SPECIAL", label: "Especial" },
  { value: "PRIVATE", label: "Privado" },
];

const statusOptions = [
  { value: "DRAFT", label: "Borrador" },
  { value: "PUBLISHED", label: "Publicado" },
  { value: "ARCHIVED", label: "Archivado" },
];
</script>

<template>
  <UModal>
    <!-- TRIGGER - Botón integrado en el componente -->
    <UButton
      :label="isEditing ? 'Editar Tour' : 'Agregar Tour'"
      :trailing-icon="isEditing ? 'i-lucide-pencil' : 'i-lucide-plus'"
      color="primary"
      class="shrink-0"
    />

    <!-- CONTENIDO DEL MODAL -->
    <template #content>
      <UCard class="w-full max-w-2xl mx-auto">
        <template #header>
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                {{ isEditing ? "Editar Tour" : "Crear Nuevo Tour" }}
              </h3>
              <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
                {{
                  isEditing
                    ? "Modifica la información del tour"
                    : "Completa la información para crear un nuevo tour"
                }}
              </p>
            </div>
            <UButton
              color="gray"
              variant="ghost"
              icon="i-heroicons-x-mark-20-solid"
              class="-my-1"
            />
          </div>
        </template>

        <UForm
          ref="form"
          :schema="schema"
          :state="state"
          class="space-y-6"
          @submit="onSubmit"
        >
          <!-- Información Básica -->
          <div class="space-y-4">
            <h4
              class="text-sm font-medium text-gray-900 dark:text-white border-b pb-2"
            >
              Información Básica
            </h4>

            <UFormGroup label="Nombre del Tour" name="name" required>
              <UInput
                v-model="state.name"
                placeholder="Ej: Tour Astronómico Premium en el Desierto"
                size="lg"
                :ui="{ base: 'w-full' }"
              />
            </UFormGroup>

            <UFormGroup label="Descripción" name="description" required>
              <UTextarea
                v-model="state.description"
                placeholder="Describe la experiencia, incluyendo actividades, puntos de interés y qué hace especial este tour..."
                :rows="4"
                :ui="{ base: 'w-full' }"
              />
            </UFormGroup>
          </div>

          <!-- Categoría y Estado -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <UFormGroup label="Categoría" name="category" required>
              <USelectMenu
                v-model="state.category"
                :options="categoryOptions"
                placeholder="Selecciona una categoría"
                size="lg"
                :ui="{ base: 'w-full' }"
                option-attribute="label"
              />
            </UFormGroup>

            <UFormGroup label="Estado" name="status" required>
              <USelectMenu
                v-model="state.status"
                :options="statusOptions"
                placeholder="Selecciona un estado"
                size="lg"
                :ui="{ base: 'w-full' }"
                option-attribute="label"
              />
            </UFormGroup>
          </div>

          <!-- Precios -->
          <div class="space-y-4">
            <h4
              class="text-sm font-medium text-gray-900 dark:text-white border-b pb-2"
            >
              Precios (CLP)
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <UFormGroup label="Precio Adulto" name="priceAdult" required>
                <UInput
                  v-model.number="state.priceAdult"
                  type="number"
                  min="0"
                  placeholder="0"
                  size="lg"
                  :ui="{ base: 'w-full' }"
                >
                  <template #leading>
                    <span class="text-gray-500">$</span>
                  </template>
                </UInput>
              </UFormGroup>

              <UFormGroup label="Precio Niño (Opcional)" name="priceChild">
                <UInput
                  v-model.number="state.priceChild"
                  type="number"
                  min="0"
                  placeholder="0"
                  size="lg"
                  :ui="{ base: 'w-full' }"
                >
                  <template #leading>
                    <span class="text-gray-500">$</span>
                  </template>
                </UInput>
              </UFormGroup>
            </div>
          </div>

          <!-- Configuración -->
          <div class="space-y-4">
            <h4
              class="text-sm font-medium text-gray-900 dark:text-white border-b pb-2"
            >
              Configuración
            </h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <UFormGroup
                label="Máximo de Participantes"
                name="defaultMaxParticipants"
                required
              >
                <UInput
                  v-model.number="state.defaultMaxParticipants"
                  type="number"
                  min="1"
                  max="100"
                  placeholder="10"
                  size="lg"
                  :ui="{ base: 'w-full' }"
                >
                  <template #trailing>
                    <span class="text-gray-500">personas</span>
                  </template>
                </UInput>
              </UFormGroup>

              <UFormGroup label="Duración" name="durationHours" required>
                <UInput
                  v-model.number="state.durationHours"
                  type="number"
                  min="1"
                  max="24"
                  placeholder="2"
                  size="lg"
                  :ui="{ base: 'w-full' }"
                >
                  <template #trailing>
                    <span class="text-gray-500">horas</span>
                  </template>
                </UInput>
              </UFormGroup>
            </div>
          </div>
        </UForm>

        <template #footer>
          <div class="flex justify-between items-center">
            <div class="text-sm text-gray-500 dark:text-gray-400">
              {{
                isEditing ? "Modificando tour existente" : "Creando nuevo tour"
              }}
            </div>
            <div class="flex gap-3">
              <UButton label="Cancelar" color="gray" variant="ghost" />
              <UButton
                :label="isEditing ? 'Guardar Cambios' : 'Crear Tour'"
                color="primary"
                :loading="loading"
                @click="form?.submit()"
                :disabled="loading"
              />
            </div>
          </div>
        </template>
      </UCard>
    </template>
  </UModal>
</template>
