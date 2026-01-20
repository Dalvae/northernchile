// composables/useAdminTourForm.ts
import { z } from 'zod'
import type { FormSubmitEvent, FormErrorEvent, FormError } from '@nuxt/ui'
import type { TourRes, TourCreateReq, TourUpdateReq, ContentBlock, ItineraryItem } from 'api-client'

const DRAFT_STORAGE_KEY = 'tour-form-draft'

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

// Zod schemas for structured content types
const itineraryItemSchema = z.object({
  time: z.string().optional(),
  description: z.string().optional()
})

const contentBlockSchema = z.object({
  type: z.string().optional(),
  content: z.string().optional(),
  items: z.array(z.string()).optional()
})

// Extended schema
export const fullSchema = schema.extend({
  guideName: z.string().optional().or(z.literal('')),
  itineraryTranslations: z.record(z.string(), z.array(itineraryItemSchema)).optional(),
  equipmentTranslations: z.record(z.string(), z.array(z.string())).optional(),
  additionalInfoTranslations: z.record(z.string(), z.array(z.string())).optional(),
  descriptionBlocksTranslations: z.record(z.string(), z.array(contentBlockSchema)).optional()
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

// Helper to ensure LocalTime strings have seconds for backend compatibility
const HH_MM_LENGTH = 5 // "HH:mm" format length
const formatTimeForBackend = (time?: string): string | undefined => {
  if (!time) return undefined
  // If HH:mm format, add :00 for seconds
  return time.length === HH_MM_LENGTH ? `${time}:00` : time
}

export const useAdminTourForm = (props: { tour?: TourRes | null }, emit: (event: 'success') => void) => {
  const { localTimeToString } = useDateTime()
  const { createAdminTour, updateAdminTour } = useAdminData()
  const toast = useToast()
  const loading = ref(false)
  const formErrors = ref<FormError[]>([])
  const hasDraft = ref(false)

  // Reactive state
  const state = reactive<TourSchema>({ ...initialState })

  // LocalStorage helpers (only for create mode)
  const saveDraft = () => {
    if (import.meta.client && !props.tour) {
      try {
        localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(state))
      } catch {
        // Ignore storage errors
      }
    }
  }

  const loadDraft = (): TourSchema | null => {
    if (import.meta.client) {
      try {
        const saved = localStorage.getItem(DRAFT_STORAGE_KEY)
        if (saved) {
          return JSON.parse(saved) as TourSchema
        }
      } catch {
        // Ignore parse errors
      }
    }
    return null
  }

  const clearDraft = () => {
    if (import.meta.client) {
      try {
        localStorage.removeItem(DRAFT_STORAGE_KEY)
        hasDraft.value = false
      } catch {
        // Ignore storage errors
      }
    }
  }

  const discardDraft = () => {
    clearDraft()
    Object.assign(state, { ...initialState })
    toast.add({
      title: 'Borrador descartado',
      color: 'neutral',
      icon: 'i-heroicons-trash'
    })
  }

  // Auto-restore draft on mount (create mode only)
  onMounted(() => {
    if (!props.tour) {
      const draft = loadDraft()
      if (draft && draft.nameTranslations?.es) {
        Object.assign(state, draft)
        hasDraft.value = true
      }
    }
  })

  // Auto-save draft on state changes (create mode only, debounced)
  let saveTimeout: ReturnType<typeof setTimeout> | null = null
  watch(
    () => state,
    () => {
      if (!props.tour) {
        if (saveTimeout) clearTimeout(saveTimeout)
        saveTimeout = setTimeout(saveDraft, 1000) // Save after 1s of inactivity
      }
    },
    { deep: true }
  )

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
            contentKey: tour.contentKey || tour.slug || '',
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
        defaultStartTime: formatTimeForBackend(event.data.defaultStartTime),
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
        clearDraft() // Clear draft on successful create
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

  // Export current state as JSON
  const exportToJson = () => {
    const exportData = { ...state }
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const tourName = state.nameTranslations?.es || state.contentKey || 'tour'
    const safeName = tourName.toLowerCase().replace(/[^a-z0-9]+/g, '-').substring(0, 50)
    a.download = `tour-${safeName}-${Date.now()}.json`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    toast.add({
      title: 'JSON exportado',
      description: 'El archivo se ha descargado correctamente.',
      color: 'success',
      icon: 'i-heroicons-arrow-down-tray'
    })
  }

  // Relaxed schema for import validation (allows partial data)
  const importSchema = z.object({
    nameTranslations: z.object({
      es: z.string().optional().default(''),
      en: z.string().optional().default(''),
      pt: z.string().optional().default('')
    }).optional(),
    category: z.string().optional(),
    moonSensitive: z.boolean().optional(),
    windSensitive: z.boolean().optional(),
    cloudSensitive: z.boolean().optional(),
    price: z.number().optional(),
    defaultMaxParticipants: z.number().optional(),
    durationHours: z.number().optional(),
    defaultStartTime: z.string().optional(),
    status: z.enum(['DRAFT', 'PUBLISHED', 'ARCHIVED']).optional(),
    contentKey: z.string().optional(),
    guideName: z.string().optional(),
    recurring: z.boolean().optional(),
    recurrenceRule: z.string().optional(),
    itineraryTranslations: z.record(z.string(), z.array(itineraryItemSchema)).optional(),
    equipmentTranslations: z.record(z.string(), z.array(z.string())).optional(),
    additionalInfoTranslations: z.record(z.string(), z.array(z.string())).optional(),
    descriptionBlocksTranslations: z.record(z.string(), z.array(contentBlockSchema)).optional()
  })

  // Import JSON from file
  const importFromJson = (file: File): Promise<boolean> => {
    return new Promise((resolve) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const rawData = JSON.parse(e.target?.result as string)

          // Validate structure using Zod
          const parseResult = importSchema.safeParse(rawData)
          if (!parseResult.success) {
            const errorMessages = parseResult.error.issues
              .slice(0, 3)
              .map(err => `${String(err.path.join('.'))}: ${err.message}`)
              .join(', ')
            toast.add({
              title: 'JSON inválido',
              description: `Estructura incorrecta: ${errorMessages}`,
              color: 'error',
              icon: 'i-heroicons-exclamation-triangle'
            })
            resolve(false)
            return
          }

          const data = parseResult.data

          // Check that at least nameTranslations or category exists (basic requirement)
          if (!data.nameTranslations && !data.category) {
            toast.add({
              title: 'JSON inválido',
              description: 'El archivo no contiene la estructura de tour esperada (falta nameTranslations y category).',
              color: 'error',
              icon: 'i-heroicons-exclamation-triangle'
            })
            resolve(false)
            return
          }

          // Merge imported data with state (only defined values)
          Object.assign(state, {
            nameTranslations: data.nameTranslations || initialState.nameTranslations,
            moonSensitive: data.moonSensitive ?? false,
            windSensitive: data.windSensitive ?? false,
            cloudSensitive: data.cloudSensitive ?? false,
            category: data.category || initialState.category,
            price: data.price || initialState.price,
            defaultMaxParticipants: data.defaultMaxParticipants || initialState.defaultMaxParticipants,
            durationHours: data.durationHours || initialState.durationHours,
            defaultStartTime: data.defaultStartTime || undefined,
            status: data.status || initialState.status,
            contentKey: data.contentKey || '',
            guideName: data.guideName || undefined,
            recurring: data.recurring ?? false,
            recurrenceRule: data.recurrenceRule || undefined,
            itineraryTranslations: data.itineraryTranslations || undefined,
            equipmentTranslations: data.equipmentTranslations || undefined,
            additionalInfoTranslations: data.additionalInfoTranslations || undefined,
            descriptionBlocksTranslations: data.descriptionBlocksTranslations || undefined
          })
          toast.add({
            title: 'JSON importado',
            description: 'Los datos se han cargado correctamente.',
            color: 'success',
            icon: 'i-heroicons-arrow-up-tray'
          })
          resolve(true)
        } catch {
          toast.add({
            title: 'Error al leer JSON',
            description: 'El archivo no es un JSON válido.',
            color: 'error',
            icon: 'i-heroicons-exclamation-triangle'
          })
          resolve(false)
        }
      }
      reader.onerror = () => {
        toast.add({
          title: 'Error al leer archivo',
          description: 'No se pudo leer el archivo seleccionado.',
          color: 'error',
          icon: 'i-heroicons-exclamation-triangle'
        })
        resolve(false)
      }
      reader.readAsText(file)
    })
  }

  return {
    state,
    schema: fullSchema,
    loading,
    formErrors,
    onSubmit,
    onError,
    // Draft management
    hasDraft,
    discardDraft,
    // JSON import/export
    exportToJson,
    importFromJson
  }
}
