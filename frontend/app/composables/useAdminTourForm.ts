// composables/useAdminTourForm.ts
import { z } from 'zod'
import type { FormSubmitEvent, FormErrorEvent, FormError } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq, ContentBlock } from 'api-client'

// Definimos interfaces locales si es necesario
export interface StructuredContent {
  guideName?: string
  itineraryTranslations?: Record<string, { time: string; description: string }[]>
  equipmentTranslations?: Record<string, string[]>
  additionalInfoTranslations?: Record<string, string[]>
  descriptionBlocksTranslations?: Record<string, ContentBlock[]>
}

// 1. Esquema Zod (Copiado de tu código original)
const schema = z.object({
  nameTranslations: z.object({
    es: z.string().min(3, 'El nombre (ES) debe tener al menos 3 caracteres'),
    en: z.string().min(3, 'El nombre (EN) debe tener al menos 3 caracteres'),
    pt: z.string().min(3, 'El nombre (PT) debe tener al menos 3 caracteres')
  }),
  imageUrls: z.array(z.string().url('Debe ser una URL válida')).optional(),
  moonSensitive: z.boolean(),
  windSensitive: z.boolean(),
  cloudSensitive: z.boolean(),
  recurring: z.boolean(),
  category: z.string().min(1, 'La categoría es requerida'),
  price: z.number().min(1, 'El precio debe ser mayor a 0'),
  defaultMaxParticipants: z
    .number()
    .int()
    .min(1, 'Debe ser al menos 1 participante'),
  durationHours: z.number().int().min(1, 'Debe ser al menos 1 hora'),
  defaultStartTime: z.string().optional(),
  status: z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']),
  contentKey: z.string().min(1, 'La clave de contenido es requerida')
}).passthrough()

// Esquema extendido
export const fullSchema = schema.extend({
  guideName: z.string().optional().or(z.literal('')),
  itineraryTranslations: z.any().optional(),
  equipmentTranslations: z.any().optional(),
  additionalInfoTranslations: z.any().optional(),
  descriptionBlocksTranslations: z.any().optional()
})

export type TourSchema = z.output<typeof fullSchema> & StructuredContent

const initialState: TourSchema = {
  nameTranslations: { es: '', en: '', pt: '' },
  imageUrls: [],
  moonSensitive: false,
  windSensitive: false,
  cloudSensitive: false,
  recurring: false,
  category: 'ASTRONOMICAL',
  price: 1,
  defaultMaxParticipants: 10,
  durationHours: 2,
  defaultStartTime: undefined,
  status: 'DRAFT',
  contentKey: '',
  guideName: undefined,
  itineraryTranslations: undefined,
  equipmentTranslations: undefined,
  additionalInfoTranslations: undefined,
  descriptionBlocksTranslations: undefined
}

export const useAdminTourForm = (props: { tour?: TourRes | null }, emit: any) => {
  const { createAdminTour, updateAdminTour } = useAdminData()
  const toast = useToast()
  const loading = ref(false)
  const formErrors = ref<FormError[]>([])

  // Estado reactivo
  const state = reactive<TourSchema>({ ...initialState })

  // Watch para rellenar datos al editar o resetear cuando se crea uno nuevo
  watch(
    () => props.tour,
    (tour) => {
      // Usar nextTick para asegurar que el DOM está listo
      nextTick(() => {
        if (tour) {
          Object.assign(state, {
            nameTranslations:
              tour.nameTranslations || initialState.nameTranslations,
            imageUrls: tour.images?.map((img) => img.imageUrl || '').filter(Boolean) || [],
            moonSensitive: tour.moonSensitive || false,
            windSensitive: tour.windSensitive || false,
            cloudSensitive: tour.cloudSensitive || false,
            recurring: tour.recurring ?? false,
            category: tour.category,
            price: tour.price,
            defaultMaxParticipants: tour.defaultMaxParticipants,
            durationHours: tour.durationHours,
            defaultStartTime: (tour as any).defaultStartTime || undefined,
            status: tour.status,
            contentKey: tour.contentKey || '',
            guideName: (tour as any).guideName || undefined,
            itineraryTranslations: (tour as any).itineraryTranslations || undefined,
            equipmentTranslations: (tour as any).equipmentTranslations || undefined,
            additionalInfoTranslations: (tour as any).additionalInfoTranslations || undefined,
            descriptionBlocksTranslations: (tour as any).descriptionBlocksTranslations || undefined
          })
        } else {
          // Resetear completamente el estado
          Object.keys(state).forEach(key => {
            delete (state as any)[key]
          })
          Object.assign(state, { ...initialState })
        }
      })
    },
    { immediate: true, deep: true }
  )

  // Lógica de Submit
  const onSubmit = async (event: FormSubmitEvent<TourSchema>) => {
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

      // Clean description blocks
      const cleanDescriptionBlocks = event.data.descriptionBlocksTranslations
        ? Object.fromEntries(
            Object.entries(event.data.descriptionBlocksTranslations)
              // Filter out blocks with empty content
              .map(([lang, blocks]) => [
                lang,
                blocks.filter(block => block.content?.trim())
              ])
              // Only include languages that have at least one block
              .filter(([_, blocks]) => blocks.length > 0)
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
        additionalInfoTranslations: Object.keys(cleanAdditionalInfo || {}).length > 0 ? cleanAdditionalInfo : undefined,
        descriptionBlocksTranslations: Object.keys(cleanDescriptionBlocks || {}).length > 0 ? cleanDescriptionBlocks : undefined
      }

      if (props.tour?.id) {
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

  const onError = (event: FormErrorEvent) => {
    formErrors.value = event.errors
    toast.add({
      title: 'Error de validación',
      description: 'Por favor, corrige los campos marcados.',
      color: 'error',
      icon: 'i-heroicons-exclamation-circle'
    })
  }

  return {
    state,
    schema: fullSchema,
    loading,
    formErrors,
    onSubmit,
    onError
  }
}