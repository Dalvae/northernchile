<script setup lang="ts">
import { z } from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";
import type { TourRes, TourCreateReq, TourUpdateReq } from "~/lib/api-client";

const props = defineProps<{
  modelValue: boolean;
  tour?: TourRes | null;
}>();

const emit = defineEmits(["update:modelValue", "success"]);

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});

const isEditing = computed(() => !!props.tour);

// Schema actualizado con todas las funcionalidades del TourModal
const schema = z.object({
  nameTranslations: z.object({
    es: z.string().min(3, "El nombre (ES) debe tener al menos 3 caracteres"),
    en: z.string().min(3, "El nombre (EN) debe tener al menos 3 caracteres"),
    pt: z.string().min(3, "El nombre (PT) debe tener al menos 3 caracteres"),
  }),
  descriptionTranslations: z.object({
    es: z
      .string()
      .min(10, "La descripción (ES) debe tener al menos 10 caracteres"),
    en: z
      .string()
      .min(10, "La descripción (EN) debe tener al menos 10 caracteres"),
    pt: z
      .string()
      .min(10, "La descripción (PT) debe tener al menos 10 caracteres"),
  }),
  imageUrls: z.array(z.string().url("Debe ser una URL válida")).optional(),
  isMoonSensitive: z.boolean(),
  isWindSensitive: z.boolean(),
  isCloudSensitive: z.boolean(),
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
  nameTranslations: { es: "", en: "", pt: "" },
  descriptionTranslations: { es: "", en: "", pt: "" },
  imageUrls: [],
  isMoonSensitive: false,
  isWindSensitive: false,
  isCloudSensitive: false,
  category: "ASTRONOMICAL",
  priceAdult: 0,
  priceChild: null,
  defaultMaxParticipants: 10,
  durationHours: 2,
  status: "DRAFT",
};

const state = reactive<Schema>({ ...initialState });
const form = ref();

// Para manejar imageUrls como string temporal
const imageUrlsString = ref("");

// Configurar estado basado en props.tour
watch(
  () => props.tour,
  (tour) => {
    if (tour) {
      state.nameTranslations = tour.nameTranslations || {
        es: "",
        en: "",
        pt: "",
      };
      state.descriptionTranslations = tour.descriptionTranslations || {
        es: "",
        en: "",
        pt: "",
      };
      state.imageUrls = tour.images?.map((img) => img.imageUrl) || [];
      imageUrlsString.value = state.imageUrls.join(", ");
      state.isMoonSensitive = tour.isMoonSensitive || false;
      state.isWindSensitive = tour.isWindSensitive || false;
      state.isCloudSensitive = tour.isCloudSensitive || false;
      state.category = tour.category;
      state.priceAdult = tour.priceAdult;
      state.priceChild = tour.priceChild;
      state.defaultMaxParticipants = tour.defaultMaxParticipants;
      state.durationHours = tour.durationHours;
      state.status = tour.status;
    } else {
      // Resetear a valores por defecto
      Object.assign(state, initialState);
      imageUrlsString.value = "";
    }
  },
  { immediate: true, deep: true },
);

// Watch para convertir string de imageUrls a array
watch(imageUrlsString, (newValue) => {
  if (newValue.trim()) {
    state.imageUrls = newValue
      .split(",")
      .map((url) => url.trim())
      .filter((url) => url.length > 0);
  } else {
    state.imageUrls = [];
  }
});

const { createAdminTour, updateAdminTour } = useAdminData();
const toast = useToast();
const loading = ref(false);

async function onSubmit(event: FormSubmitEvent<Schema>) {
  console.log("Form submitted with data:", event.data);
  loading.value = true;
  try {
    const data = event.data;

    const payload = {
      ...data,
      nameTranslations: data.nameTranslations,
      descriptionTranslations: data.descriptionTranslations,
      imageUrls: data.imageUrls,
      isMoonSensitive: data.isMoonSensitive,
      isWindSensitive: data.isWindSensitive,
      isCloudSensitive: data.isCloudSensitive,
    };

    if (isEditing.value && props.tour?.id) {
      await updateAdminTour(props.tour.id, payload as TourUpdateReq);
      toast.add({
        title: "Tour actualizado con éxito",
        color: "green",
        icon: "i-heroicons-check-circle",
      });
    } else {
      await createAdminTour(payload as TourCreateReq);
      toast.add({
        title: "Tour creado con éxito",
        color: "green",
        icon: "i-heroicons-check-circle",
      });
    }
    emit("success");
    isOpen.value = false;
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
  <USlideover v-model="isOpen" :title="isEditing ? 'Editar Tour' : 'Crear Nuevo Tour'">
    <template #content>
      <!-- Header con botón de cerrar personalizado -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-neutral-200 dark:border-neutral-700">
        <h3 class="text-base font-semibold leading-6 text-neutral-900 dark:text-white">
          {{ isEditing ? "Editar Tour" : "Crear Nuevo Tour" }}
        </h3>
        <UButton
          color="neutral"
          variant="ghost"
          icon="i-heroicons-x-mark-20-solid"
          class="-my-1"
          @click="isOpen = false"
        />
      </div>

      <!-- Body con el formulario -->
      <div class="flex-1 overflow-y-auto px-6 py-4">
        <UForm
          :schema="schema"
          :state="state"
          class="space-y-4"
          @submit="onSubmit"
        >
          <UFormGroup label="Nombre del Tour" name="name">
            <UInput
              v-model="state.name"
              placeholder="Ej: Tour Astronómico Premium"
            />
          </UFormGroup>

          <UFormGroup label="Descripción" name="description">
            <UTextarea
              v-model="state.description"
              autoresize
              placeholder="Describe la experiencia del tour..."
            />
          </UFormGroup>

          <UFormGroup label="Categoría" name="category">
            <USelectMenu
              v-model="state.category"
              :options="['ASTRONOMICAL', 'REGULAR', 'SPECIAL', 'PRIVATE']"
            />
          </UFormGroup>

          <div class="grid grid-cols-2 gap-4">
            <UFormGroup label="Precio Adulto" name="priceAdult">
              <UInput
                v-model.number="state.priceAdult"
                type="number"
                icon="i-lucide-dollar-sign"
              />
            </UFormGroup>
            <UFormGroup label="Precio Niño (Opcional)" name="priceChild">
              <UInput
                v-model.number="state.priceChild"
                type="number"
                icon="i-lucide-dollar-sign"
              />
            </UFormGroup>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <UFormGroup label="Max. Participantes" name="defaultMaxParticipants">
              <UInput
                v-model.number="state.defaultMaxParticipants"
                type="number"
              />
            </UFormGroup>
            <UFormGroup label="Duración (Horas)" name="durationHours">
              <UInput v-model.number="state.durationHours" type="number" />
            </UFormGroup>
          </div>

          <UFormGroup v-if="isEditing" label="Estado" name="status">
            <USelectMenu
              v-model="state.status"
              :options="['DRAFT', 'PUBLISHED', 'ARCHIVED']"
            />
          </UFormGroup>

          <div class="flex justify-end gap-3 pt-4">
            <UButton
              label="Cancelar"
              color="neutral"
              variant="ghost"
              @click="isOpen = false"
            />
            <UButton
              type="submit"
              :label="isEditing ? 'Guardar Cambios' : 'Crear Tour'"
              color="primary"
              :loading="loading"
            />
          </div>
        </UForm>
      </div>
    </template>
  </USlideover>
</template>
