<script setup lang="ts">
import type { TourSchema } from '~/composables/useAdminTourForm'
import type { ContentBlock } from 'api-client'

const state = inject<TourSchema>('tour-form-state')!

// Helper para inicializar arrays si no existen
function ensureArray<T>(obj: Record<string, T[]>, lang: string): T[] {
  if (!obj[lang]) obj[lang] = []
  return obj[lang]
}

// Helpers de Itinerario
// El time se comparte entre idiomas, solo la descripción se traduce
const addItineraryItem = () => {
  if (!state.itineraryTranslations) {
    state.itineraryTranslations = { es: [], en: [], pt: [] }
  }
  // Añade item en todos los idiomas con el mismo índice
  const newItem = { time: '', description: '' }
  state.itineraryTranslations.es = state.itineraryTranslations.es || []
  state.itineraryTranslations.en = state.itineraryTranslations.en || []
  state.itineraryTranslations.pt = state.itineraryTranslations.pt || []
  state.itineraryTranslations.es.push({ ...newItem })
  state.itineraryTranslations.en.push({ ...newItem })
  state.itineraryTranslations.pt.push({ ...newItem })
}

const removeItineraryItem = (index: number) => {
  // Elimina el item en todos los idiomas
  state.itineraryTranslations?.es?.splice(index, 1)
  state.itineraryTranslations?.en?.splice(index, 1)
  state.itineraryTranslations?.pt?.splice(index, 1)
}

// Sincroniza el time desde ES a otros idiomas
const syncItineraryTime = (index: number, time: string) => {
  if (state.itineraryTranslations?.en?.[index]) {
    state.itineraryTranslations.en[index].time = time
  }
  if (state.itineraryTranslations?.pt?.[index]) {
    state.itineraryTranslations.pt[index].time = time
  }
}

// Helpers de Equipamiento
const addEquipmentItem = (lang: string) => {
  if (!state.equipmentTranslations) {
    state.equipmentTranslations = { es: [], en: [], pt: [] }
  }
  ensureArray(state.equipmentTranslations, lang).push('')
}

const removeEquipmentItem = (lang: string, index: number) => {
  if (state.equipmentTranslations?.[lang]) {
    state.equipmentTranslations[lang].splice(index, 1)
  }
}

// Helpers de Información Adicional
const addAdditionalInfoItem = (lang: string) => {
  if (!state.additionalInfoTranslations) {
    state.additionalInfoTranslations = { es: [], en: [], pt: [] }
  }
  ensureArray(state.additionalInfoTranslations, lang).push('')
}

const removeAdditionalInfoItem = (lang: string, index: number) => {
  if (state.additionalInfoTranslations?.[lang]) {
    state.additionalInfoTranslations[lang].splice(index, 1)
  }
}

// Helper para asegurar que descriptionBlocksTranslations esté inicializado
const ensureDescriptionBlocks = (lang: string): ContentBlock[] => {
  if (!state.descriptionBlocksTranslations) {
    state.descriptionBlocksTranslations = { es: [], en: [], pt: [] }
  }
  if (!state.descriptionBlocksTranslations[lang]) {
    state.descriptionBlocksTranslations[lang] = []
  }
  return state.descriptionBlocksTranslations[lang]
}
</script>

<template>
  <div class="space-y-8">
    <!-- Clave de Contenido -->
    <UFormField
      label="Clave de Contenido"
      name="contentKey"
      required
    >
      <UInput
        v-model="state.contentKey"
        placeholder="Ej: tour-astronomico-premium"
        size="lg"
        class="w-full"
      />
    </UFormField>

    <!-- Bloques de Contenido Estructurado -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
        Bloques de Contenido Estructurado
      </h4>
      <p class="text-sm text-neutral-500 dark:text-neutral-300">
        Crea contenido estructurado con títulos, párrafos y listas para una mejor presentación.
      </p>
      <UTabs
        :items="[
          { label: 'Español', slot: 'blocks-es' },
          { label: 'Inglés', slot: 'blocks-en' },
          { label: 'Portugués', slot: 'blocks-pt' }
        ]"
        class="w-full"
      >
        <template #blocks-es>
          <div class="pt-4">
            <AdminToursContentBlockEditor
              :model-value="ensureDescriptionBlocks('es')"
              language="es"
              @update:model-value="(value) => {
                ensureDescriptionBlocks('es')
                ensureDescriptionBlocks('es')
                state.descriptionBlocksTranslations!.es = value
              }"
            />
          </div>
        </template>
        <template #blocks-en>
          <div class="pt-4">
            <AdminToursContentBlockEditor
              :model-value="ensureDescriptionBlocks('en')"
              language="en"
              @update:model-value="(value) => {
                ensureDescriptionBlocks('en')
                ensureDescriptionBlocks('en')
                state.descriptionBlocksTranslations!.en = value
              }"
            />
          </div>
        </template>
        <template #blocks-pt>
          <div class="pt-4">
            <AdminToursContentBlockEditor
              :model-value="ensureDescriptionBlocks('pt')"
              language="pt"
              @update:model-value="(value) => {
                ensureDescriptionBlocks('pt')
                ensureDescriptionBlocks('pt')
                state.descriptionBlocksTranslations!.pt = value
              }"
            />
          </div>
        </template>
      </UTabs>
    </div>

    <!-- Guía (Opcional) -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
        Guía (Opcional)
      </h4>
      <UFormField
        label="Nombre del Guía"
        name="guideName"
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

    <!-- Itinerario (Opcional) -->
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
              <UFormField
                label="Hora"
                class="w-32"
              >
                <UInput
                  v-model="item.time"
                  placeholder="19:30"
                  size="md"
                  @update:model-value="syncItineraryTime(index, $event as string)"
                />
              </UFormField>
              <UFormField
                label="Descripción"
                class="flex-1"
              >
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
                @click="removeItineraryItem(index)"
              />
            </div>
            <UButton
              label="Añadir Item"
              icon="i-heroicons-plus"
              color="primary"
              variant="outline"
              @click="addItineraryItem()"
            />
          </div>
        </template>
        <template #itinerary-en>
          <div class="pt-4 space-y-4">
            <p v-if="!state.itineraryTranslations?.es?.length" class="text-sm text-muted">
              Primero añade items en Español
            </p>
            <div
              v-for="(item, index) in state.itineraryTranslations?.en || []"
              :key="index"
              class="flex gap-3 items-start"
            >
              <UFormField
                label="Time"
                class="w-32"
              >
                <UInput
                  :model-value="state.itineraryTranslations?.es?.[index]?.time || ''"
                  disabled
                  size="md"
                  class="opacity-60"
                />
              </UFormField>
              <UFormField
                label="Description"
                class="flex-1"
              >
                <UInput
                  v-model="item.description"
                  placeholder="Reception with infusions..."
                  size="md"
                />
              </UFormField>
            </div>
          </div>
        </template>
        <template #itinerary-pt>
          <div class="pt-4 space-y-4">
            <p v-if="!state.itineraryTranslations?.es?.length" class="text-sm text-muted">
              Primeiro adicione itens em Espanhol
            </p>
            <div
              v-for="(item, index) in state.itineraryTranslations?.pt || []"
              :key="index"
              class="flex gap-3 items-start"
            >
              <UFormField
                label="Hora"
                class="w-32"
              >
                <UInput
                  :model-value="state.itineraryTranslations?.es?.[index]?.time || ''"
                  disabled
                  size="md"
                  class="opacity-60"
                />
              </UFormField>
              <UFormField
                label="Descrição"
                class="flex-1"
              >
                <UInput
                  v-model="item.description"
                  placeholder="Recepção com infusões..."
                  size="md"
                />
              </UFormField>
            </div>
          </div>
        </template>
      </UTabs>
    </div>

    <!-- Equipamiento (Opcional) -->
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

    <!-- Información Adicional (Opcional) -->
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
  </div>
</template>
