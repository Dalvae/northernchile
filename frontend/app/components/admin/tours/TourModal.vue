<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent, FormErrorEvent, FormError } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq } from '~/lib/api-client'

const props = defineProps<{
  tour?: TourRes | null
  open?: boolean // ✅ Añade esto
}>()

const emit = defineEmits<{
  'success': []
  'close': [] // ✅ Añade esto
  'update:open': [value: boolean] // ✅ Añade esto
}>()

const isEditing = computed(() => !!props.tour)

// ✅ Control local del modal
const isOpen = computed({
  get: () => props.open ?? false,
  set: value => emit('update:open', value)
})

const schema = z.object({
  nameTranslations: z.object({
    es: z.string().min(3, 'El nombre (ES) debe tener al menos 3 caracteres'),
    en: z.string().min(3, 'El nombre (EN) debe tener al menos 3 caracteres'),
    pt: z.string().min(3, 'El nombre (PT) debe tener al menos 3 caracteres')
  }),
  descriptionTranslations: z.object({
    es: z
      .string()
      .min(10, 'La descripción (ES) debe tener al menos 10 caracteres'),
    en: z
      .string()
      .min(10, 'La descripción (EN) debe tener al menos 10 caracteres'),
    pt: z
      .string()
      .min(10, 'La descripción (PT) debe tener al menos 10 caracteres')
  }),
  imageUrls: z.array(z.string().url('Debe ser una URL válida')).optional(),
  moonSensitive: z.boolean(),
  windSensitive: z.boolean(),
  cloudSensitive: z.boolean(),
  category: z.string().min(1, 'La categoría es requerida'),
  price: z.number().min(1, 'El precio debe ser mayor a 0'),
  defaultMaxParticipants: z
    .number()
    .int()
    .min(1, 'Debe ser al menos 1 participante'),
  durationHours: z.number().int().min(1, 'Debe ser al menos 1 hora'),
  status: z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']),
  contentKey: z.string().min(1, 'La clave de contenido es requerida')
})

type Schema = z.output<typeof schema>

const initialState: Schema = {
  nameTranslations: { es: '', en: '', pt: '' },
  descriptionTranslations: { es: '', en: '', pt: '' },
  imageUrls: [],
  moonSensitive: false,
  windSensitive: false,
  cloudSensitive: false,
  category: 'ASTRONOMICAL',
  price: 1,
  defaultMaxParticipants: 10,
  durationHours: 2,
  status: 'DRAFT',
  contentKey: ''
}

const state = reactive<Schema>({ ...initialState })
const form = ref()

// Variable para guardar los errores de validación
const formErrors = ref<FormError[]>([])

// Funciones para manejar imágenes
const handleImageUploaded = (data: { key: string, url: string }) => {
  if (!state.imageUrls) {
    state.imageUrls = []
  }
  state.imageUrls.push(data.url)
}

const removeImage = (index: number) => {
  if (state.imageUrls) {
    state.imageUrls.splice(index, 1)
  }
}

watch(
  () => props.tour,
  (tour) => {
    if (tour) {
      Object.assign(state, {
        nameTranslations:
          tour.nameTranslations || initialState.nameTranslations,
        descriptionTranslations:
          tour.descriptionTranslations || initialState.descriptionTranslations,
        imageUrls: tour.images?.map(img => img.imageUrl) || [],
        moonSensitive: tour.moonSensitive || false,
        windSensitive: tour.windSensitive || false,
        cloudSensitive: tour.cloudSensitive || false,
        category: tour.category,
        price: tour.price,
        defaultMaxParticipants: tour.defaultMaxParticipants,
        durationHours: tour.durationHours,
        status: tour.status,
        contentKey: tour.contentKey || ''
      })
    } else {
      Object.assign(state, initialState)
    }
  },
  { immediate: true, deep: true }
)

const { createAdminTour, updateAdminTour } = useAdminData()
const toast = useToast()
const loading = ref(false)

// onError ahora guarda los errores en nuestra variable local
function onError(event: FormErrorEvent) {
  formErrors.value = event.errors
  toast.add({
    title: 'Error de validación',
    description: 'Por favor, corrige los campos marcados.',
    color: 'error',
    icon: 'i-heroicons-exclamation-circle'
  })
}

// onSubmit ahora limpia los errores antes de empezar
async function onSubmit(event: FormSubmitEvent<Schema>) {
  formErrors.value = [] // Limpiar errores al intentar enviar
  loading.value = true
  try {
    const payload = { ...event.data }

    if (isEditing.value && props.tour?.id) {
      await updateAdminTour(props.tour.id, payload as TourUpdateReq)
      toast.add({
        title: 'Tour actualizado con éxito',
        color: 'success',
        icon: 'i-heroicons-check-circle'
      })
    } else {
      await createAdminTour(payload as TourCreateReq)
      toast.add({
        title: 'Tour creado con éxito',
        color: 'success',
        icon: 'i-heroicons-check-circle'
      })
    }
    emit('success')
  } catch (error: any) {
    const description
      = error.data?.message || error.message || 'No se pudo guardar el tour'
    toast.add({
      title: 'Error al guardar',
      description,
      color: 'error',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    loading.value = false
  }
}

function handleSubmit() {
  form.value?.submit()
}

// Función para encontrar el error de un campo específico
const findError = (path: string) =>
  formErrors.value.find(e => e.path === path)?.message

const categoryOptions = [
  { label: 'Astronómico', value: 'ASTRONOMICAL' },
  { label: 'Regular', value: 'REGULAR' },
  { label: 'Especial', value: 'SPECIAL' },
  { label: 'Privado', value: 'PRIVATE' }
]

const statusOptions = [
  { label: 'Borrador', value: 'DRAFT' },
  { label: 'Publicado', value: 'PUBLISHED' },
  { label: 'Archivado', value: 'ARCHIVED' }
]
</script>

<template>
  <UModal v-model:open="isOpen">
    <template #content>
      <div class="flex flex-col h-full">
        <div
          class="flex items-center justify-between p-5 pb-4 border-b border-neutral-200 dark:border-neutral-700 flex-shrink-0"
        >
          <div>
            <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
              {{ isEditing ? "Editar Tour" : "Crear Nuevo Tour" }}
            </h3>
            <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
              {{
                isEditing
                  ? "Modifica la información del tour"
                  : "Completa la información para crear un nuevo tour"
              }}
            </p>
          </div>
          <!-- ✅ Botón de cerrar que emite el evento -->
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-heroicons-x-mark-20-solid"
            class="-my-1"
            @click="emit('close')"
          />
        </div>
        <div class="flex-1 overflow-y-auto max-h-[60vh]">
          <div class="p-5">
            <div class="space-y-8">
              <UForm
                ref="form"
                :schema="schema"
                :state="state"
                class="space-y-8"
                @submit="onSubmit"
                @error="onError"
              >
                <div class="space-y-6">
                  <h4
                    class="text-base font-semibold text-neutral-900 dark:text-white border-b border-neutral-200 dark:border-neutral-700 pb-2"
                  >
                    Información Básica
                  </h4>
                  <UTabs
                    :items="[
                      { label: 'Español', slot: 'es' },
                      { label: 'Inglés', slot: 'en' },
                      { label: 'Portugués', slot: 'pt' }
                    ]"
                    class="w-full"
                  >
                    <template #es="{ item }">
                      <div class="pt-4 space-y-6">
                        <UFormField
                          label="Nombre (ES)"
                          name="nameTranslations.es"
                          required
                          :error="findError('nameTranslations.es')"
                        >
                          <UInput
                            v-model="state.nameTranslations.es"
                            placeholder="Ej: Tour Astronómico Premium en el Desierto"
                            size="lg"
                            class="w-full"
                          />
                        </UFormField>
                        <UFormField
                          label="Descripción (ES)"
                          name="descriptionTranslations.es"
                          required
                          :error="findError('descriptionTranslations.es')"
                        >
                          <UTextarea
                            v-model="state.descriptionTranslations.es"
                            placeholder="Describe la experiencia..."
                            :rows="4"
                            class="w-full min-h-[100px]"
                          />
                        </UFormField>
                      </div>
                    </template>
                    <template #en="{ item }">
                      <div class="pt-4 space-y-6">
                        <UFormField
                          label="Nombre (EN)"
                          name="nameTranslations.en"
                          required
                          :error="findError('nameTranslations.en')"
                        >
                          <UInput
                            v-model="state.nameTranslations.en"
                            placeholder="Ej: Premium Astronomical Tour in the Desert"
                            size="lg"
                            class="w-full"
                          />
                        </UFormField>
                        <UFormField
                          label="Descripción (EN)"
                          name="descriptionTranslations.en"
                          required
                          :error="findError('descriptionTranslations.en')"
                        >
                          <UTextarea
                            v-model="state.descriptionTranslations.en"
                            placeholder="Describe the experience..."
                            :rows="4"
                            class="w-full min-h-[100px]"
                          />
                        </UFormField>
                      </div>
                    </template>
                    <template #pt="{ item }">
                      <div class="pt-4 space-y-6">
                        <UFormField
                          label="Nombre (PT)"
                          name="nameTranslations.pt"
                          required
                          :error="findError('nameTranslations.pt')"
                        >
                          <UInput
                            v-model="state.nameTranslations.pt"
                            placeholder="Ex: Tour Astronômico Premium no Deserto"
                            size="lg"
                            class="w-full"
                          />
                        </UFormField>
                        <UFormField
                          label="Descripción (PT)"
                          name="descriptionTranslations.pt"
                          required
                          :error="findError('descriptionTranslations.pt')"
                        >
                          <UTextarea
                            v-model="state.descriptionTranslations.pt"
                            placeholder="Descreva a experiência..."
                            :rows="4"
                            class="w-full min-h-[100px]"
                          />
                        </UFormField>
                      </div>
                    </template>
                  </UTabs>
                  <UFormField
                    label="Clave de Contenido"
                    name="contentKey"
                    required
                    :error="findError('contentKey')"
                  >
                    <UInput
                      v-model="state.contentKey"
                      placeholder="Ej: tour-astronomico-premium"
                      size="lg"
                      class="w-full"
                    />
                  </UFormField>
                  <UFormField
                    label="Imágenes del Tour"
                    name="imageUrls"
                    :error="findError('imageUrls')"
                  >
                    <div class="space-y-4">
                      <!-- Mostrar imágenes actuales -->
                      <div
                        v-if="state.imageUrls && state.imageUrls.length > 0"
                        class="grid grid-cols-2 gap-4"
                      >
                        <div
                          v-for="(url, index) in state.imageUrls"
                          :key="index"
                          class="relative group"
                        >
                          <img
                            :src="url"
                            :alt="`Tour image ${index + 1}`"
                            class="w-full h-32 object-cover rounded-lg border border-neutral-700"
                          >
                          <UButton
                            icon="i-heroicons-x-mark"
                            color="error"
                            size="xs"
                            class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                            @click="removeImage(index)"
                          />
                        </div>
                      </div>

                      <!-- Uploader de nueva imagen -->
                      <CommonImageUploader
                        folder="tours"
                        @uploaded="handleImageUploaded"
                      />
                    </div>
                    <template #help>
                      <p class="text-xs text-neutral-500 mt-1">
                        Sube imágenes del tour. Máximo 5MB por imagen.
                      </p>
                    </template>
                  </UFormField>
                </div>
                <div class="space-y-4">
                  <h4
                    class="text-base font-semibold text-neutral-900 dark:text-white border-b border-neutral-200 dark:border-neutral-700 pb-2"
                  >
                    Condiciones Meteorológicas
                  </h4>
                  <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <UCheckbox
                      v-model="state.windSensitive"
                      label="Sensible al Viento"
                      name="windSensitive"
                    />
                    <UCheckbox
                      v-model="state.moonSensitive"
                      label="Sensible a la Luna"
                      name="moonSensitive"
                    />
                    <UCheckbox
                      v-model="state.cloudSensitive"
                      label="Sensible a la Nubosidad"
                      name="cloudSensitive"
                    />
                  </div>
                </div>
                <div class="space-y-4">
                  <h4
                    class="text-base font-semibold text-neutral-900 dark:text-white border-b border-neutral-200 dark:border-neutral-700 pb-2"
                  >
                    Clasificación
                  </h4>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <UFormField
                      label="Categoría"
                      name="category"
                      required
                      :error="findError('category')"
                    >
                      <USelect
                        v-model="state.category"
                        :items="categoryOptions"
                        option-attribute="label"
                        value-attribute="value"
                        size="lg"
                        class="w-full"
                      />
                    </UFormField>
                    <UFormField
                      label="Estado"
                      name="status"
                      required
                      :error="findError('status')"
                    >
                      <USelect
                        v-model="state.status"
                        :items="statusOptions"
                        option-attribute="label"
                        value-attribute="value"
                        size="lg"
                        class="w-full"
                      />
                    </UFormField>
                  </div>
                </div>
                <div class="space-y-4">
                  <h4
                    class="text-base font-semibold text-neutral-900 dark:text-white border-b border-neutral-200 dark:border-neutral-700 pb-2"
                  >
                    Precios (CLP)
                  </h4>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <UFormField
                      label="Precio"
                      name="price"
                      required
                      :error="findError('price')"
                    >
                      <UInput
                        v-model.number="state.price"
                        type="number"
                        min="1"
                        size="lg"
                        class="w-full"
                      />
                    </UFormField>
                  </div>
                </div>
                <div class="space-y-4">
                  <h4
                    class="text-base font-semibold text-neutral-900 dark:text-white border-b border-neutral-200 dark:border-neutral-700 pb-2"
                  >
                    Configuración
                  </h4>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <UFormField
                      label="Máximo de Participantes"
                      name="defaultMaxParticipants"
                      required
                      :error="findError('defaultMaxParticipants')"
                    >
                      <UInput
                        v-model.number="state.defaultMaxParticipants"
                        type="number"
                        min="1"
                        max="100"
                        size="lg"
                        class="w-null"
                      />
                    </UFormField>
                    <UFormField
                      label="Duración (Horas)"
                      name="durationHours"
                      required
                      :error="findError('durationHours')"
                    >
                      <UInput
                        v-model.number="state.durationHours"
                        type="number"
                        min="1"
                        max="24"
                        size="lg"
                        class="w-full"
                      />
                    </UFormField>
                  </div>
                </div>
              </UForm>
            </div>
          </div>
        </div>
        <div
          class="flex justify-between items-center p-5 pt-4 border-t border-neutral-200 dark:border-neutral-700 flex-shrink-0"
        >
          <div class="text-sm text-neutral-500 dark:text-neutral-400">
            {{
              isEditing ? "Modificando tour existente" : "Creando nuevo tour"
            }}
          </div>
          <div class="flex gap-3">
            <!-- ✅ Botón cancelar cierra el modal -->
            <UButton
              label="Cancelar"
              color="neutral"
              variant="ghost"
              :disabled="loading"
              @click="emit('close')"
            />
            <UButton
              :label="isEditing ? 'Guardar Cambios' : 'Crear Tour'"
              color="primary"
              :loading="loading"
              @click="handleSubmit"
            />
          </div>
        </div>
      </div>
    </template>
  </UModal>
</template>
