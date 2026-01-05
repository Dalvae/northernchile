<script setup lang="ts">
import type { FormError } from '@nuxt/ui'
import type { TourSchema } from '~/composables/useAdminTourForm'
import CronEditor from './CronEditor.vue'

const state = inject<TourSchema>('tour-form-state')!
const errors = inject<Ref<FormError[]>>('tour-form-errors')!

const findError = (path: string) =>
  errors.value?.find(e => e.name === path)?.message

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
  <div class="space-y-8">
    <!-- SECCIÓN 1: INFORMACIÓN BÁSICA (Nombres) -->
    <div>
      <h4
        class="text-base font-semibold text-default border-b border-default pb-2 mb-4"
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
        <template #es>
          <div class="pt-4">
            <UFormField
              label="Nombre (ES)"
              name="nameTranslations.es"
              required
              :error="findError('nameTranslations.es')"
            >
              <UInput
                v-model="state.nameTranslations.es"
                placeholder="Ej: Tour Astronómico Premium"
                size="lg"
                class="w-full"
              />
            </UFormField>
          </div>
        </template>
        <template #en>
          <div class="pt-4">
            <UFormField
              label="Nombre (EN)"
              name="nameTranslations.en"
              required
              :error="findError('nameTranslations.en')"
            >
              <UInput
                v-model="state.nameTranslations.en"
                placeholder="Ex: Premium Astronomical Tour"
                size="lg"
                class="w-full"
              />
            </UFormField>
          </div>
        </template>
        <template #pt>
          <div class="pt-4">
            <UFormField
              label="Nombre (PT)"
              name="nameTranslations.pt"
              required
              :error="findError('nameTranslations.pt')"
            >
              <UInput
                v-model="state.nameTranslations.pt"
                placeholder="Ex: Tour Astronômico Premium"
                size="lg"
                class="w-full"
              />
            </UFormField>
          </div>
        </template>
      </UTabs>
    </div>

    <!-- SECCIÓN 2: DETALLES OPERATIVOS (Agrupamos Clasificación, Precio y Logística) -->
    <div>
      <h4
        class="text-base font-semibold text-default border-b border-default pb-2 mb-4"
      >
        Detalles Operativos
      </h4>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Fila 1: Datos Comerciales -->
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

        <UFormField
          label="Precio (CLP)"
          name="price"
          required
          :error="findError('price')"
        >
          <UInput
            v-model.number="state.price"
            type="number"
            min="1"
            size="lg"
            icon="i-heroicons-currency-dollar"
            class="w-full"
          />
        </UFormField>

        <!-- Fila 2: Logística -->
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
            icon="i-heroicons-clock"
            class="w-full"
          />
        </UFormField>

        <UFormField
          label="Máx. Participantes"
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
            icon="i-heroicons-users"
            class="w-full"
          />
        </UFormField>

        <!-- HORA DE INICIO: Solo visible si NO es recurrente -->
        <UFormField
          v-if="!state.recurring"
          label="Hora de Inicio"
          name="defaultStartTime"
          :error="findError('defaultStartTime')"
          help="Horario por defecto para agendamiento manual"
        >
          <UInput
            v-model="state.defaultStartTime"
            type="time"
            size="lg"
            class="w-full"
          />
        </UFormField>
      </div>
    </div>

    <!-- SECCIÓN 3: CONDICIONES CLIMÁTICAS -->
    <div>
      <h4
        class="text-base font-semibold text-default border-b border-default pb-2 mb-4"
      >
        Restricciones Climáticas
      </h4>
      <div
        class="flex flex-wrap gap-6 p-4 bg-neutral-50 dark:bg-neutral-900 rounded-lg border border-neutral-200 dark:border-neutral-800"
      >
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

    <!-- SECCIÓN 4: PROGRAMACIÓN Y RECURRENCIA -->
    <div>
      <h4
        class="text-base font-semibold text-default border-b border-default pb-2 mb-4"
      >
        Programación Automática
      </h4>

      <div class="space-y-4">
        <div
          class="flex items-center justify-between p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-neutral-50 dark:bg-neutral-900"
        >
          <div>
            <p class="text-sm font-medium text-default">
              Activar Recurrencia
            </p>
            <p class="text-xs text-muted">
              Generar horarios automáticamente (ej. todos los días a las 20:00)
            </p>
          </div>
          <UFormField name="recurring">
            <USwitch
              v-model="state.recurring"
              size="lg"
            />
          </UFormField>
        </div>

        <!-- Editor Cron: Solo visible si es recurrente -->
        <div
          v-if="state.recurring"
          class="animate-fade-in pl-4 border-l-2 border-primary-500"
        >
          <UFormField
            name="recurrenceRule"
            :error="findError('recurrenceRule')"
          >
            <CronEditor v-model="state.recurrenceRule as string" />
          </UFormField>
        </div>
      </div>
    </div>
  </div>
</template>
