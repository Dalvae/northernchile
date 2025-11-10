<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent, FormErrorEvent, FormError } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq } from 'api-client'

const props = defineProps<{
  tour?: TourRes | null
  open?: boolean
}>()

const emit = defineEmits<{
  'success': []
  'close': []
  'update:open': [value: boolean]
}>()

const isEditing = computed(() => !!props.tour)

const isOpen = computed({
  get: () => props.open ?? false,
  set: value => emit('update:open', value)
})

// Define types for structured content
interface ItineraryItem {
  time: string
  description: string
}

interface StructuredContent {
  guideName?: string
  itineraryTranslations?: Record<string, ItineraryItem[]>
  equipmentTranslations?: Record<string, string[]>
  additionalInfoTranslations?: Record<string, string[]>
}

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
}).passthrough() // Allow extra fields that we'll clean up in onSubmit

// Extend schema to allow optional structured content without validation
const fullSchema = schema.extend({
  guideName: z.string().optional().or(z.literal('')),
  itineraryTranslations: z.any().optional(),
  equipmentTranslations: z.any().optional(),
  additionalInfoTranslations: z.any().optional()
})

type Schema = z.output<typeof fullSchema> & StructuredContent

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
  contentKey: '',
  guideName: undefined,
  itineraryTranslations: undefined,
  equipmentTranslations: undefined,
  additionalInfoTranslations: undefined
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
        imageUrls: tour.images?.map((img) => img.imageUrl || '').filter(Boolean) || [],
        moonSensitive: tour.moonSensitive || false,
        windSensitive: tour.windSensitive || false,
        cloudSensitive: tour.cloudSensitive || false,
        category: tour.category,
        price: tour.price,
        defaultMaxParticipants: tour.defaultMaxParticipants,
        durationHours: tour.durationHours,
        status: tour.status,
        contentKey: tour.contentKey || '',
        guideName: (tour as any).guideName || undefined,
        itineraryTranslations: (tour as any).itineraryTranslations || undefined,
        equipmentTranslations: (tour as any).equipmentTranslations || undefined,
        additionalInfoTranslations: (tour as any).additionalInfoTranslations || undefined
      })
    } else {
      Object.assign(state, { ...initialState })
    }
  },
  { immediate: true, deep: true }
)

const { createAdminTour, updateAdminTour } = useAdminData()
const toast = useToast()
const loading = ref(false)

// Helper functions for structured content management
function addItineraryItem(lang: string) {
  if (!state.itineraryTranslations) {
    state.itineraryTranslations = { es: [], en: [], pt: [] }
  }
  if (!state.itineraryTranslations[lang]) {
    state.itineraryTranslations[lang] = []
  }
  state.itineraryTranslations[lang].push({ time: '', description: '' })
}

function removeItineraryItem(lang: string, index: number) {
  if (state.itineraryTranslations?.[lang]) {
    state.itineraryTranslations[lang].splice(index, 1)
  }
}

function addEquipmentItem(lang: string) {
  if (!state.equipmentTranslations) {
    state.equipmentTranslations = { es: [], en: [], pt: [] }
  }
  if (!state.equipmentTranslations[lang]) {
    state.equipmentTranslations[lang] = []
  }
  state.equipmentTranslations[lang].push('')
}

function removeEquipmentItem(lang: string, index: number) {
  if (state.equipmentTranslations?.[lang]) {
    state.equipmentTranslations[lang].splice(index, 1)
  }
}

function addAdditionalInfoItem(lang: string) {
  if (!state.additionalInfoTranslations) {
    state.additionalInfoTranslations = { es: [], en: [], pt: [] }
  }
  if (!state.additionalInfoTranslations[lang]) {
    state.additionalInfoTranslations[lang] = []
  }
  state.additionalInfoTranslations[lang].push('')
}

function removeAdditionalInfoItem(lang: string, index: number) {
  if (state.additionalInfoTranslations?.[lang]) {
    state.additionalInfoTranslations[lang].splice(index, 1)
  }
}

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
    // Clean up empty structured content before sending
    const cleanItinerary = event.data.itineraryTranslations
      ? Object.fromEntries(
          Object.entries(event.data.itineraryTranslations)
            // Filter out languages with empty or invalid items
            .map(([lang, items]) => [
              lang,
              items.filter(item => item.time?.trim() && item.description?.trim())
            ])
            // Only include languages that have at least one valid item
            .filter(([_, items]) => items.length > 0)
        )
      : undefined

    const cleanEquipment = event.data.equipmentTranslations
      ? Object.fromEntries(
          Object.entries(event.data.equipmentTranslations)
            // Filter empty strings in each language
            .map(([lang, items]) => [lang, items.filter(item => item?.trim())])
            // Only include languages that have at least one item
            .filter(([_, items]) => items.length > 0)
        )
      : undefined

    const cleanAdditionalInfo = event.data.additionalInfoTranslations
      ? Object.fromEntries(
          Object.entries(event.data.additionalInfoTranslations)
            // Filter empty strings in each language
            .map(([lang, items]) => [lang, items.filter(item => item?.trim())])
            // Only include languages that have at least one item
            .filter(([_, items]) => items.length > 0)
        )
      : undefined

    const basePayload: TourCreateReq = {
      ...event.data,
      imageUrls:
        event.data.imageUrls && event.data.imageUrls.length > 0
          ? event.data.imageUrls
          : undefined,
      guideName: event.data.guideName?.trim() || undefined,
      itineraryTranslations: Object.keys(cleanItinerary || {}).length > 0 ? cleanItinerary : undefined,
      equipmentTranslations: Object.keys(cleanEquipment || {}).length > 0 ? cleanEquipment : undefined,
      additionalInfoTranslations: Object.keys(cleanAdditionalInfo || {}).length > 0 ? cleanAdditionalInfo : undefined
    }

    if (isEditing.value && props.tour?.id) {
      const updatePayload: TourUpdateReq = { ...basePayload }
      await updateAdminTour(props.tour.id, updatePayload)
      toast.add({
        title: 'Tour actualizado con éxito',
        color: 'success',
        icon: 'i-heroicons-check-circle'
      })
    } else {
      await createAdminTour(basePayload)
      toast.add({
        title: 'Tour creado con éxito',
        color: 'success',
        icon: 'i-heroicons-check-circle'
      })
    }
    emit('success')
  } catch (error: unknown) {
    let description = 'No se pudo guardar el tour'

    if (typeof error === 'string') {
      description = error
    } else if (error && typeof error === 'object') {
      const anyError = error as { data?: any; message?: string }
      description
        = anyError.data?.message
        || anyError.message
        || description
    }

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
  formErrors.value.find(e => (e as any).path === path)?.message

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
          class="flex items-center justify-between p-5 pb-4 border-b border-default flex-shrink-0"
        >
          <div>
            <h3 class="text-lg font-semibold text-default">
              {{ isEditing ? "Editar Tour" : "Crear Nuevo Tour" }}
            </h3>
            <p class="text-sm text-muted mt-1">
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
                :schema="fullSchema"
                :state="state"
                class="space-y-8"
                @submit="onSubmit"
                @error="onError"
              >
                <div class="space-y-6">
                  <h4
                     class="text-base font-semibold text-default border-b border-default pb-2"
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
                             class="w-full h-32 object-cover rounded-lg border border-default"
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
                       <p class="text-xs text-muted mt-1">
                        Sube imágenes del tour. Máximo 5MB por imagen.
                      </p>
                    </template>
                  </UFormField>
                </div>
                <div class="space-y-4">
                  <h4
                     class="text-base font-semibold text-default border-b border-default pb-2"
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
                     class="text-base font-semibold text-default border-b border-default pb-2"
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
                     class="text-base font-semibold text-default border-b border-default pb-2"
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
                     class="text-base font-semibold text-default border-b border-default pb-2"
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

                <!-- Guide Name (Optional) -->
                <div class="space-y-4">
                  <h4 class="text-base font-semibold text-default border-b border-default pb-2">
                    Guía (Opcional)
                  </h4>
                  <UFormField
                    label="Nombre del Guía"
                    name="guideName"
                    :error="findError('guideName')"
                  >
                    <UInput
                      v-model="state.guideName"
                      placeholder="Ej: Alex, David"
                      size="lg"
                      class="w-full"
                    />
                    <template #help>
                      <p class="text-xs text-muted mt-1">
                        Nombre del guía para este tour (opcional)
                      </p>
                    </template>
                  </UFormField>
                </div>

                <!-- Itinerary Editor -->
                <div class="space-y-4">
                  <h4 class="text-base font-semibold text-default border-b border-default pb-2">
                    Itinerario (Opcional)
                  </h4>
                  <UTabs
                    :items="[
                      { label: 'Español', slot: 'itinerary-es' },
                      { label: 'Inglés', slot: 'itinerary-en' },
                      { label: 'Portugués', slot: 'itinerary-pt' }
                    ]"
                    class="w-full"
                  >
                    <template #itinerary-es>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.itineraryTranslations?.es || []"
                          :key="index"
                          class="flex gap-3 items-start"
                        >
                          <UFormField label="Hora" class="w-32">
                            <UInput
                              v-model="item.time"
                              placeholder="19:30"
                              size="md"
                            />
                          </UFormField>
                          <UFormField label="Descripción" class="flex-1">
                            <UInput
                              v-model="item.description"
                              placeholder="Recepción con infusiones..."
                              size="md"
                            />
                          </UFormField>
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            class="mt-6"
                            @click="removeItineraryItem('es', index)"
                          />
                        </div>
                        <UButton
                          label="Añadir Item"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addItineraryItem('es')"
                        />
                      </div>
                    </template>
                    <template #itinerary-en>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.itineraryTranslations?.en || []"
                          :key="index"
                          class="flex gap-3 items-start"
                        >
                          <UFormField label="Time" class="w-32">
                            <UInput
                              v-model="item.time"
                              placeholder="7:30 PM"
                              size="md"
                            />
                          </UFormField>
                          <UFormField label="Description" class="flex-1">
                            <UInput
                              v-model="item.description"
                              placeholder="Reception with infusions..."
                              size="md"
                            />
                          </UFormField>
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            class="mt-6"
                            @click="removeItineraryItem('en', index)"
                          />
                        </div>
                        <UButton
                          label="Add Item"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addItineraryItem('en')"
                        />
                      </div>
                    </template>
                    <template #itinerary-pt>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.itineraryTranslations?.pt || []"
                          :key="index"
                          class="flex gap-3 items-start"
                        >
                          <UFormField label="Hora" class="w-32">
                            <UInput
                              v-model="item.time"
                              placeholder="19:30"
                              size="md"
                            />
                          </UFormField>
                          <UFormField label="Descrição" class="flex-1">
                            <UInput
                              v-model="item.description"
                              placeholder="Recepção com infusões..."
                              size="md"
                            />
                          </UFormField>
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            class="mt-6"
                            @click="removeItineraryItem('pt', index)"
                          />
                        </div>
                        <UButton
                          label="Adicionar Item"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addItineraryItem('pt')"
                        />
                      </div>
                    </template>
                  </UTabs>
                </div>

                <!-- Equipment Editor -->
                <div class="space-y-4">
                  <h4 class="text-base font-semibold text-default border-b border-default pb-2">
                    Equipamiento (Opcional)
                  </h4>
                  <UTabs
                    :items="[
                      { label: 'Español', slot: 'equipment-es' },
                      { label: 'Inglés', slot: 'equipment-en' },
                      { label: 'Portugués', slot: 'equipment-pt' }
                    ]"
                    class="w-full"
                  >
                    <template #equipment-es>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.equipmentTranslations?.es || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.equipmentTranslations!.es![index]"
                            placeholder="Ej: Telescopio Celestron 8SE"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeEquipmentItem('es', index)"
                          />
                        </div>
                        <UButton
                          label="Añadir Equipamiento"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addEquipmentItem('es')"
                        />
                      </div>
                    </template>
                    <template #equipment-en>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.equipmentTranslations?.en || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.equipmentTranslations!.en![index]"
                            placeholder="Ex: Celestron 8SE Telescope"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeEquipmentItem('en', index)"
                          />
                        </div>
                        <UButton
                          label="Add Equipment"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addEquipmentItem('en')"
                        />
                      </div>
                    </template>
                    <template #equipment-pt>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.equipmentTranslations?.pt || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.equipmentTranslations!.pt![index]"
                            placeholder="Ex: Telescópio Celestron 8SE"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeEquipmentItem('pt', index)"
                          />
                        </div>
                        <UButton
                          label="Adicionar Equipamento"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addEquipmentItem('pt')"
                        />
                      </div>
                    </template>
                  </UTabs>
                </div>

                <!-- Additional Info Editor -->
                <div class="space-y-4">
                  <h4 class="text-base font-semibold text-default border-b border-default pb-2">
                    Información Adicional (Opcional)
                  </h4>
                  <UTabs
                    :items="[
                      { label: 'Español', slot: 'info-es' },
                      { label: 'Inglés', slot: 'info-en' },
                      { label: 'Portugués', slot: 'info-pt' }
                    ]"
                    class="w-full"
                  >
                    <template #info-es>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.additionalInfoTranslations?.es || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.additionalInfoTranslations!.es![index]"
                            placeholder="Ej: Lleva ropa abrigada"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeAdditionalInfoItem('es', index)"
                          />
                        </div>
                        <UButton
                          label="Añadir Información"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addAdditionalInfoItem('es')"
                        />
                      </div>
                    </template>
                    <template #info-en>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.additionalInfoTranslations?.en || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.additionalInfoTranslations!.en![index]"
                            placeholder="Ex: Bring warm clothes"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeAdditionalInfoItem('en', index)"
                          />
                        </div>
                        <UButton
                          label="Add Information"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addAdditionalInfoItem('en')"
                        />
                      </div>
                    </template>
                    <template #info-pt>
                      <div class="pt-4 space-y-4">
                        <div
                          v-for="(item, index) in state.additionalInfoTranslations?.pt || []"
                          :key="index"
                          class="flex gap-3"
                        >
                          <UInput
                            v-model="state.additionalInfoTranslations!.pt![index]"
                            placeholder="Ex: Traga roupas quentes"
                            size="md"
                            class="flex-1"
                          />
                          <UButton
                            icon="i-heroicons-trash"
                            color="error"
                            variant="ghost"
                            size="md"
                            @click="removeAdditionalInfoItem('pt', index)"
                          />
                        </div>
                        <UButton
                          label="Adicionar Informação"
                          icon="i-heroicons-plus"
                          color="primary"
                          variant="outline"
                          @click="addAdditionalInfoItem('pt')"
                        />
                      </div>
                    </template>
                  </UTabs>
                </div>
              </UForm>
            </div>
          </div>
        </div>
         <div
          class="flex justify-between items-center p-5 pt-4 border-t border-default flex-shrink-0"
        >
           <div class="text-sm text-muted">
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
