<script setup lang="ts">
import { z } from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";
import type { TourRes, TourCreateReq, TourUpdateReq } from "~/lib/api-client";

const props = defineProps<{
  tour?: TourRes | null;
}>();

const emit = defineEmits<{
  success: [];
  close: [];
}>();

const isEditing = computed(() => !!props.tour);
const isOpen = ref(false);

// Schema con mensajes de error en español
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
  imageUrls: z
    .array(z.string().url("Debe ser una URL válida"))
    .min(1, "Al menos una imagen es requerida"),
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

// Submit function
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

function closeModal() {
  isOpen.value = false;
  emit("close");
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
  <UModal
    v-model="isOpen"
    :ui="{
      base: 'relative text-left rtl:text-right overflow-visible my-4',
      container:
        'flex min-h-full items-center sm:items-center justify-center p-4',
      overlay: 'overflow-y-auto',
      content: 'max-w-4xl w-full mx-auto max-h-[90vh] flex flex-col',
    }"
  >
    <!-- TRIGGER - Botón integrado en el componente -->
    <UButton
      :label="isEditing ? 'Editar Tour' : 'Agregar Tour'"
      :trailing-icon="isEditing ? 'i-lucide-pencil' : 'i-lucide-plus'"
      color="primary"
      class="shrink-0"
      @click="isOpen = true"
    />

    <!-- CONTENIDO DEL MODAL -->
    <UCard class="flex flex-col h-full">
      <!-- HEADER -->
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
            @click="closeModal"
          />
        </div>
      </template>

      <!-- CONTENIDO SCROLLEABLE -->
      <div class="flex-1 overflow-y-auto px-6">
        <div class="space-y-8 py-4">
          <UForm ref="form" :schema="schema" :state="state" @submit="onSubmit">
            <!-- Información Básica -->
            <div class="space-y-6">
              <h4
                class="text-base font-semibold text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700 pb-2"
              >
                Información Básica
              </h4>

              <UTabs
                :items="[
                  { label: 'Español', slot: 'es' },
                  { label: 'Inglés', slot: 'en' },
                  { label: 'Portugués', slot: 'pt' },
                ]"
                class="w-full"
              >
                <template #es>
                  <div class="pt-4 space-y-6">
                    <UFormGroup
                      label="Nombre (ES)"
                      name="nameTranslations.es"
                      required
                    >
                      <UInput
                        v-model="state.nameTranslations.es"
                        placeholder="Ej: Tour Astronómico Premium en el Desierto"
                        size="lg"
                        class="w-full"
                      />
                    </UFormGroup>

                    <UFormGroup
                      label="Descripción (ES)"
                      name="descriptionTranslations.es"
                      required
                    >
                      <UTextarea
                        v-model="state.descriptionTranslations.es"
                        placeholder="Describe la experiencia, incluyendo actividades, puntos de interés y qué hace especial este tour..."
                        :rows="4"
                        class="w-full min-h-[100px]"
                      />
                    </UFormGroup>
                  </div>
                </template>

                <template #en>
                  <div class="pt-4 space-y-6">
                    <UFormGroup
                      label="Nombre (EN)"
                      name="nameTranslations.en"
                      required
                    >
                      <UInput
                        v-model="state.nameTranslations.en"
                        placeholder="Ej: Premium Astronomical Tour in the Desert"
                        size="lg"
                        class="w-full"
                      />
                    </UFormGroup>

                    <UFormGroup
                      label="Descripción (EN)"
                      name="descriptionTranslations.en"
                      required
                    >
                      <UTextarea
                        v-model="state.descriptionTranslations.en"
                        placeholder="Describe the experience, including activities, points of interest and what makes this tour special..."
                        :rows="4"
                        class="w-full min-h-[100px]"
                      />
                    </UFormGroup>
                  </div>
                </template>

                <template #pt>
                  <div class="pt-4 space-y-6">
                    <UFormGroup
                      label="Nombre (PT)"
                      name="nameTranslations.pt"
                      required
                    >
                      <UInput
                        v-model="state.nameTranslations.pt"
                        placeholder="Ex: Tour Astronômico Premium no Deserto"
                        size="lg"
                        class="w-full"
                      />
                    </UFormGroup>

                    <UFormGroup
                      label="Descripción (PT)"
                      name="descriptionTranslations.pt"
                      required
                    >
                      <UTextarea
                        v-model="state.descriptionTranslations.pt"
                        placeholder="Descreva a experiência, incluindo atividades, pontos de interesse e o que torna este passeio especial..."
                        :rows="4"
                        class="w-full min-h-[100px]"
                      />
                    </UFormGroup>
                  </div>
                </template>
              </UTabs>

              <UFormGroup
                label="URLs de Imágenes (separadas por comas)"
                name="imageUrls"
                required
              >
                <UTextarea
                  v-model="imageUrlsString"
                  placeholder="https://example.com/image1.jpg, https://example.com/image2.png"
                  :rows="3"
                  class="w-full"
                />
                <template #help>
                  <p class="text-xs text-gray-500 mt-1">
                    Separe las URLs con comas. Mínimo 1 imagen requerida.
                  </p>
                </template>
              </UFormGroup>
            </div>

            <!-- Reglas de Negocio -->
            <div class="space-y-4">
              <h4
                class="text-base font-semibold text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700 pb-2"
              >
                Condiciones Meteorológicas
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <UCheckbox
                  v-model="state.isWindSensitive"
                  label="Sensible al Viento"
                  name="isWindSensitive"
                />
                <UCheckbox
                  v-model="state.isMoonSensitive"
                  label="Sensible a la Luna"
                  name="isMoonSensitive"
                />
                <UCheckbox
                  v-model="state.isCloudSensitive"
                  label="Sensible a la Nubosidad"
                  name="isCloudSensitive"
                />
              </div>
            </div>

            <!-- Categoría y Estado -->
            <div class="space-y-4">
              <h4
                class="text-base font-semibold text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700 pb-2"
              >
                Clasificación
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <UFormGroup label="Categoría" name="category" required>
                  <USelectMenu
                    v-model="state.category"
                    :options="categoryOptions"
                    placeholder="Selecciona una categoría"
                    size="lg"
                    class="w-full"
                    option-attribute="label"
                  />
                </UFormGroup>

                <UFormGroup label="Estado" name="status" required>
                  <USelectMenu
                    v-model="state.status"
                    :options="statusOptions"
                    placeholder="Selecciona un estado"
                    size="lg"
                    class="w-full"
                    option-attribute="label"
                  />
                </UFormGroup>
              </div>
            </div>

            <!-- Precios -->
            <div class="space-y-4">
              <h4
                class="text-base font-semibold text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700 pb-2"
              >
                Precios (CLP)
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <UFormGroup label="Precio Adulto" name="priceAdult" required>
                  <UInput
                    v-model.number="state.priceAdult"
                    type="number"
                    min="0"
                    placeholder="0"
                    size="lg"
                    class="w-full"
                  >
                    <template #leading>
                      <span class="text-gray-500 font-medium">$</span>
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
                    class="w-full"
                  >
                    <template #leading>
                      <span class="text-gray-500 font-medium">$</span>
                    </template>
                  </UInput>
                </UFormGroup>
              </div>
            </div>

            <!-- Configuración -->
            <div class="space-y-4">
              <h4
                class="text-base font-semibold text-gray-900 dark:text-white border-b border-gray-200 dark:border-gray-700 pb-2"
              >
                Configuración
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
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
                    class="w-full"
                  >
                    <template #trailing>
                      <span class="text-gray-500 font-medium">personas</span>
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
                    class="w-full"
                  >
                    <template #trailing>
                      <span class="text-gray-500 font-medium">horas</span>
                    </template>
                  </UInput>
                </UFormGroup>
              </div>
            </div>
          </UForm>
        </div>
      </div>

      <!-- FOOTER -->
      <template #footer>
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-500 dark:text-gray-400">
            {{
              isEditing ? "Modificando tour existente" : "Creando nuevo tour"
            }}
          </div>
          <div class="flex gap-3">
            <UButton
              label="Cancelar"
              color="gray"
              variant="ghost"
              @click="closeModal"
              :disabled="loading"
            />
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
  </UModal>
</template>
