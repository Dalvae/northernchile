// composables/useAdminTourForm.ts
import { z } from 'zod'
import type { FormSubmitEvent, FormErrorEvent, FormError } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq, ContentBlock, ItineraryItem, LocalTime } from 'api-client'

// Schema definition
const schema = z.object({
  nameTranslations: z.object({
    es: z.string().min(3, 'El nombre (ES) debe tener al menos 3 caracteres'),
    en: z.string().min(3, 'El nombre (EN) debe tener al menos 3 caracteres'),
    pt: z.string().min(3, 'El nombre (PT) debe tener al menos 3 caracteres')
  }),
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
  defaultStartTime: z.string().optional(),
  status: z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']),
  contentKey: z.string().min(1, 'La clave de contenido es requerida'),
  recurring: z.boolean().optional(),
  recurrenceRule: z.string().optional()
}).passthrough()

// Extended schema
export const fullSchema = schema.extend({
  guideName: z.string().optional().or(z.literal('')),
  itineraryTranslations: z.record(z.string(), z.array(z.any())).optional(),
  equipmentTranslations: z.record(z.string(), z.array(z.string())).optional(),
  additionalInfoTranslations: z.record(z.string(), z.array(z.string())).optional(),
  descriptionBlocksTranslations: z.record(z.string(), z.array(z.any())).optional()
})

export type TourSchema = z.output<typeof fullSchema>

const initialState: TourSchema = {
  nameTranslations: { es: '', en: '', pt: '' },
  moonSensitive: false,
  windSensitive: false,
  cloudSensitive: false,
  category: 'ASTRONOMICAL',
  price: 1,
  defaultMaxParticipants: 10,
  durationHours: 2,
  defaultStartTime: undefined,
  status: 'DRAFT',
  contentKey: '',
  guideName: undefined,
  recurring: false,
  recurrenceRule: undefined,
  itineraryTranslations: undefined,
  equipmentTranslations: undefined,
  additionalInfoTranslations: undefined,
  descriptionBlocksTranslations: undefined
}

export const useAdminTourForm = (props: { tour?: TourRes | null }, emit: (event: 'success') => void) => {
  const { localTimeToString } = useDateTime()
  const { createAdminTour, updateAdminTour } = useAdminData()
  const toast = useToast()
  const loading = ref(false)
  const formErrors = ref<FormError[]>([])

  // Reactive state
  const state = reactive<TourSchema>({ ...initialState })

  // Watch to populate data when editing or reset when creating new
  watch(
    () => props.tour,
    (tour) => {
      nextTick(() => {
        if (tour) {
          Object.assign(state, {
            nameTranslations: tour.nameTranslations || initialState.nameTranslations,
            moonSensitive: tour.isMoonSensitive ?? false,
            windSensitive: tour.isWindSensitive ?? false,
            cloudSensitive: tour.isCloudSensitive ?? false,
            category: tour.category,
            price: tour.price,
            defaultMaxParticipants: tour.defaultMaxParticipants,
            durationHours: tour.durationHours,
            defaultStartTime: localTimeToString(tour.defaultStartTime),
            status: tour.status,
            contentKey: tour.contentKey || '',
            guideName: tour.guideName || undefined,
            itineraryTranslations: tour.itineraryTranslations || undefined,
            equipmentTranslations: tour.equipmentTranslations || undefined,
            additionalInfoTranslations: tour.additionalInfoTranslations || undefined,
            descriptionBlocksTranslations: tour.descriptionBlocksTranslations || undefined
          })
        } else {
          // Reset state to initial values (simpler than delete + assign)
          Object.assign(state, { ...initialState })
        }
      })
    },
    { immediate: true, deep: true }
  )

  // Submit logic
  const onSubmit = async (event: FormSubmitEvent<TourSchema>) => {
    formErrors.value = []
    loading.value = true
    try {
      // Clean up empty structured content before sending
      const itineraryData = event.data.itineraryTranslations as Record<string, ItineraryItem[]> | undefined
      const cleanItinerary = itineraryData
        ? Object.fromEntries(
            Object.entries(itineraryData)
              .map(([lang, items]) => [
                lang,
                items.filter((item: ItineraryItem) => item.time?.trim() && item.description?.trim())
              ])
              .filter(([, items]) => (items as ItineraryItem[]).length > 0)
          )
        : undefined

      const equipmentData = event.data.equipmentTranslations as Record<string, string[]> | undefined
      const cleanEquipment = equipmentData
        ? Object.fromEntries(
            Object.entries(equipmentData)
              .map(([lang, items]) => [lang, items.filter((item: string) => item?.trim())])
              .filter(([, items]) => (items as string[]).length > 0)
          )
        : undefined

      const additionalInfoData = event.data.additionalInfoTranslations as Record<string, string[]> | undefined
      const cleanAdditionalInfo = additionalInfoData
        ? Object.fromEntries(
            Object.entries(additionalInfoData)
              .map(([lang, items]) => [lang, items.filter((item: string) => item?.trim())])
              .filter(([, items]) => (items as string[]).length > 0)
          )
        : undefined

      const descBlocksData = event.data.descriptionBlocksTranslations as Record<string, ContentBlock[]> | undefined
      const cleanDescriptionBlocks = descBlocksData
        ? Object.fromEntries(
            Object.entries(descBlocksData)
              .map(([lang, blocks]) => [
                lang,
                blocks.filter((block: ContentBlock) => block.content?.trim())
              ])
              .filter(([, blocks]) => (blocks as ContentBlock[]).length > 0)
          )
        : undefined

      // Build payload
      const basePayload: TourCreateReq = {
        nameTranslations: event.data.nameTranslations,
        descriptionBlocksTranslations: cleanDescriptionBlocks || {},
        isMoonSensitive: event.data.moonSensitive,
        isWindSensitive: event.data.windSensitive,
        isCloudSensitive: event.data.cloudSensitive,
        category: event.data.category,
        price: event.data.price,
        defaultMaxParticipants: event.data.defaultMaxParticipants,
        durationHours: event.data.durationHours,
        // Note: Backend expects LocalTime as ISO string (e.g., "10:30:00"), not object
        // The OpenAPI schema incorrectly models LocalTime as an object
        defaultStartTime: event.data.defaultStartTime as unknown as LocalTime | undefined,
        status: event.data.status,
        contentKey: event.data.contentKey,
        guideName: event.data.guideName?.trim() || undefined,
        itineraryTranslations: Object.keys(cleanItinerary || {}).length > 0 ? cleanItinerary : undefined,
        equipmentTranslations: Object.keys(cleanEquipment || {}).length > 0 ? cleanEquipment : undefined,
        additionalInfoTranslations: Object.keys(cleanAdditionalInfo || {}).length > 0 ? cleanAdditionalInfo : undefined
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
        const anyError = error as { data?: { message?: string }, message?: string }
        description = anyError.data?.message || anyError.message || description
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
