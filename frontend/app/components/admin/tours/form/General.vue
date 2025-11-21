<script setup lang="ts">
import type { FormError } from '@nuxt/ui'
import type { TourSchema } from '~/composables/useAdminTourForm'

const props = defineProps<{
  state: TourSchema
  errors: FormError[]
}>()

const findError = (path: string) =>
  props.errors.find(e => (e as any).path === path)?.message

// Opciones estáticas
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
  <div class="space-y-6">
    <!-- Información Básica -->
    <div class="space-y-6">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
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
          </div>
        </template>
      </UTabs>
    </div>

    <!-- Condiciones Meteorológicas -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
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

    <!-- [+] NUEVO BLOQUE: Switch Recurrencia -->
    <div class="p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900/50 flex items-center justify-between">
      <div>
        <p class="text-sm font-medium text-default">Tour Recurrente</p>
        <p class="text-xs text-muted">Actívalo si el tour se repite regularmente (ej. todos los días)</p>
      </div>
      <UFormField name="recurring">
        <USwitch v-model="state.recurring" size="lg" />
      </UFormField>
    </div>

    <!-- Clasificación -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
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

    <!-- Precios (CLP) -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
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

    <!-- Configuración -->
    <div class="space-y-4">
      <h4 class="text-base font-semibold text-default border-b border-default pb-2">
        Configuración
      </h4>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
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
            class="w-full"
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
        <UFormField
          label="Hora de Inicio (opcional)"
          name="defaultStartTime"
          :error="findError('defaultStartTime')"
        >
          <UInput
            v-model="state.defaultStartTime"
            type="time"
            size="lg"
            class="w-full"
            placeholder="19:30"
          />
        </UFormField>
      </div>
    </div>
  </div>
</template>