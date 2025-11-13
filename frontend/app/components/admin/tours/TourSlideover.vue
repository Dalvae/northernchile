<script setup lang="ts">
import { z } from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq } from 'api-client'

const props = defineProps<{
  modelValue: boolean
  tour?: TourRes | null
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const isOpen = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const isEditing = computed(() => !!props.tour)

// Schema actualizado con todas las funcionalidades del TourModal
const schema = z.object({
  nameTranslations: z.object({
    es: z.string().min(3, 'El nombre (ES) debe tener al menos 3 caracteres'),
    en: z.string().min(3, 'El nombre (EN) debe tener al menos 3 caracteres'),
    pt: z.string().min(3, 'El nombre (PT) debe tener al menos 3 caracteres')
  }),
  imageUrls: z.array(z.string().url('Debe ser una URL válida')).optional(),
  isMoonSensitive: z.boolean(),
  isWindSensitive: z.boolean(),
  isCloudSensitive: z.boolean(),
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

// Estado inicial con tipos correctos
const initialState: Schema = {
  nameTranslations: { es: '', en: '', pt: '' },
  imageUrls: [],
  isMoonSensitive: false,
  isWindSensitive: false,
  isCloudSensitive: false,
  category: 'ASTRONOMICAL',
  price: 0,
  defaultMaxParticipants: 10,
  durationHours: 2,
  status: 'DRAFT',
  contentKey: ''
}

const state = reactive<Schema>({ ...initialState })
const form = ref()

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

// Configurar estado basado en props.tour
watch(
  () => props.tour,
  (tour) => {
    if (tour) {
      state.nameTranslations = {
        es: tour.nameTranslations?.es || '',
        en: tour.nameTranslations?.en || '',
        pt: tour.nameTranslations?.pt || ''
      }
      state.imageUrls = tour.images?.map(img => img.imageUrl).filter((url): url is string => !!url) || []
      state.isMoonSensitive = tour.isMoonSensitive || false
      state.isWindSensitive = tour.isWindSensitive || false
      state.isCloudSensitive = tour.isCloudSensitive || false
      state.category = tour.category || 'ASTRONOMICAL'
      state.price = tour.price || 0
      state.defaultMaxParticipants = tour.defaultMaxParticipants || 10
      state.durationHours = tour.durationHours || 2
      state.status = (tour.status || 'DRAFT') as 'PUBLISHED' | 'DRAFT' | 'ARCHIVED'
      state.contentKey = tour.contentKey || ''
    } else {
      // Resetear a valores por defecto
      Object.assign(state, initialState)
    }
  },
  { immediate: true, deep: true }
)

const { createAdminTour, updateAdminTour } = useAdminData()
const toast = useToast()
const loading = ref(false)

async function onSubmit(event: FormSubmitEvent<Schema>) {
  loading.value = true
  try {
    const data = event.data

    const payload = {
      ...data,
      nameTranslations: data.nameTranslations,
      imageUrls: data.imageUrls,
      isMoonSensitive: data.isMoonSensitive,
      isWindSensitive: data.isWindSensitive,
      isCloudSensitive: data.isCloudSensitive
    }

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
    isOpen.value = false
  } catch (error: any) {
    console.error('Error submitting form:', error)
    toast.add({
      title: 'Error',
      description: error.message || 'No se pudo guardar el tour',
      color: 'error',
      icon: 'i-heroicons-exclamation-triangle'
    })
  } finally {
    loading.value = false
  }
}

// Opciones para los selects
const categoryOptions = [
  { value: 'ASTRONOMICAL', label: 'Astronómico' },
  { value: 'REGULAR', label: 'Regular' },
  { value: 'SPECIAL', label: 'Especial' },
  { value: 'PRIVATE', label: 'Privado' }
]

const statusOptions = [
  { value: 'DRAFT', label: 'Borrador' },
  { value: 'PUBLISHED', label: 'Publicado' },
  { value: 'ARCHIVED', label: 'Archivado' }
]
</script>

<template>
  <USlideover
    v-model="isOpen"
    :title="isEditing ? 'Editar Tour' : 'Crear Nuevo Tour'"
  >
    <template #content>
      <!-- Header con botón de cerrar personalizado -->
       <div class="flex items-center justify-between px-6 py-4 border-b border-default">
         <h3 class="text-base font-semibold leading-6 text-default">
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
       <div class="flex-1 overflow-y-auto px-6 py-4 bg-default">
        <UForm
          :schema="schema"
          :state="state"
          class="space-y-4"
          @submit="onSubmit"
        >
          <UFormGroup
            label="Nombre del Tour"
            name="nameTranslations.es"
          >
            <UInput
              v-model="state.nameTranslations.es"
              placeholder="Ej: Tour Astronómico Premium"
            />
          </UFormGroup>

          <UFormGroup
            label="Clave de Contenido"
            name="contentKey"
          >
            <UInput
              v-model="state.contentKey"
              placeholder="Ej: tour-astronomico-premium"
            />
          </UFormGroup>

          <UFormGroup
            label="Categoría"
            name="category"
          >
            <USelectMenu
              v-model="state.category"
              :options="['ASTRONOMICAL', 'REGULAR', 'SPECIAL', 'PRIVATE']"
            />
          </UFormGroup>

          <UFormGroup
            label="Imágenes del Tour"
            name="imageUrls"
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
              Sube imágenes del tour. Máximo 5MB por imagen.
            </template>
          </UFormGroup>

          <div class="grid grid-cols-2 gap-4">
            <UFormGroup
              label="Precio"
              name="price"
            >
              <UInput
                v-model.number="state.price"
                type="number"
                icon="i-lucide-dollar-sign"
              />
            </UFormGroup>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <UFormGroup
              label="Max. Participantes"
              name="defaultMaxParticipants"
            >
              <UInput
                v-model.number="state.defaultMaxParticipants"
                type="number"
              />
            </UFormGroup>
            <UFormGroup
              label="Duración (Horas)"
              name="durationHours"
            >
              <UInput
                v-model.number="state.durationHours"
                type="number"
              />
            </UFormGroup>
          </div>

          <UFormGroup
            v-if="isEditing"
            label="Estado"
            name="status"
          >
            <USelectMenu
              v-model="state.status"
              :options="['DRAFT', 'PUBLISHED', 'ARCHIVED']"
            />
          </UFormGroup>

          <div class="flex justify-end gap-3 pt-4 border-t border-default">
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
