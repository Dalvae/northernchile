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

const schema = z.object({
  name: z.string().min(3, "El nombre es requerido"),
  description: z.string().min(10, "La descripción es requerida"),
  category: z.string().min(3, "La categoría es requerida"),
  priceAdult: z.number().min(0, "El precio debe ser positivo"),
  priceChild: z.number().min(0, "El precio debe ser positivo"),
  defaultMaxParticipants: z.number().int().min(1, "Debe ser al menos 1"),
  durationHours: z.number().int().min(1, "Debe ser al menos 1"),
  status: z.enum(["DRAFT", "PUBLISHED", "ARCHIVED"]).optional(),
});

type Schema = z.output<typeof schema>;

const state = reactive<Partial<Schema>>({});

function setFormState(tour: TourRes | null | undefined) {
  if (tour) {
    state.name = tour.name;
    state.description = tour.description;
    state.category = tour.category;
    state.priceAdult = tour.priceAdult;
    state.priceChild = tour.priceChild;
    state.defaultMaxParticipants = tour.defaultMaxParticipants;
    state.durationHours = tour.durationHours;
    state.status = tour.status as any;
  } else {
    Object.keys(state).forEach(
      (key) => delete state[key as keyof typeof state],
    );
    state.status = "DRAFT";
  }
}

watch(() => props.tour, setFormState, { immediate: true });

const { createAdminTour, updateAdminTour } = useAdminData();
const toast = useToast();
const loading = ref(false);

async function onSubmit(event: FormSubmitEvent<Schema>) {
  loading.value = true;
  try {
    if (isEditing.value && props.tour?.id) {
      await updateAdminTour(props.tour.id, event.data as TourUpdateReq);
      toast.add({ title: "Tour actualizado con éxito", color: "green" });
    } else {
      await createAdminTour(event.data as TourCreateReq);
      toast.add({ title: "Tour creado con éxito", color: "green" });
    }
    emit("success");
    isOpen.value = false;
  } catch (error: any) {
    toast.add({
      title: "Error",
      description: error.message || "No se pudo guardar el tour",
      color: "red",
    });
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <USlideover v-model="isOpen">
    <UCard
      class="flex flex-col flex-1"
      :ui="{
        body: { base: 'flex-1' },
        ring: '',
        divide: 'divide-y divide-gray-100 dark:divide-gray-800',
      }"
    >
      <template #header>
        <div class="flex items-center justify-between">
          <h3
            class="text-base font-semibold leading-6 text-gray-900 dark:text-white"
          >
            {{ isEditing ? "Editar Tour" : "Crear Nuevo Tour" }}
          </h3>
          <UButton
            color="gray"
            variant="ghost"
            icon="i-heroicons-x-mark-20-solid"
            class="-my-1"
            @click="isOpen = false"
          />
        </div>
      </template>

      <UForm
        :schema="schema"
        :state="state"
        class="space-y-4"
        @submit="onSubmit"
      >
        <UFormGroup label="Nombre" name="name">
          <UInput v-model="state.name" />
        </UFormGroup>
        <UFormGroup label="Descripción" name="description">
          <UTextarea v-model="state.description" autoresize />
        </UFormGroup>
        <UFormGroup label="Categoría" name="category">
          <UInput v-model="state.category" />
        </UFormGroup>
        <div class="grid grid-cols-2 gap-4">
          <UFormGroup label="Precio Adulto" name="priceAdult">
            <UInput v-model.number="state.priceAdult" type="number" />
          </UFormGroup>
          <UFormGroup label="Precio Niño" name="priceChild">
            <UInput v-model.number="state.priceChild" type="number" />
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

        <div class="flex justify-end gap-3">
          <UButton
            label="Cancelar"
            color="gray"
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
    </UCard>
  </USlideover>
</template>
