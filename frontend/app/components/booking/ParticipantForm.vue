<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
          {{ t('booking.participant', { number: index + 1 }) }}
        </h3>
      </div>
    </template>

    <div class="space-y-4">
      <!-- Saved Participant Selector (only for logged-in users) -->
      <div
        v-if="isAuthenticated && savedParticipantOptions.length > 0"
        class="mb-4"
      >
        <UFormField
          :label="t('booking.saved_participant')"
          name="savedParticipant"
        >
          <USelect
            :model-value="selectedSavedParticipantId"
            :items="savedParticipantSelectItems"
            placeholder="Seleccionar participante guardado..."
            size="lg"
            icon="i-lucide-users"
            class="w-full"
            @update:model-value="handleSavedParticipantSelect($event)"
          />
        </UFormField>
      </div>
      <!-- Full Name -->
      <UFormField
        :label="t('booking.full_name')"
        name="fullName"
        required
        :help="participant.fullName && participant.fullName.length < 2 ? 'Mínimo 2 caracteres' : undefined"
      >
        <UInput
          :model-value="participant.fullName"
          :placeholder="t('booking.full_name_placeholder')"
          size="lg"
          icon="i-lucide-user"
          required
          minlength="2"
          maxlength="100"
          @update:model-value="emit('update', { fullName: $event })"
        />
      </UFormField>

      <!-- Document ID and Nationality -->
      <div class="grid md:grid-cols-2 gap-4">
        <UFormField
          :label="t('booking.document_id')"
          name="documentId"
          required
          :help="participant.documentId && participant.documentId.length < 3 ? 'Mínimo 3 caracteres' : undefined"
        >
          <UInput
            :model-value="participant.documentId"
            :placeholder="t('booking.document_id_placeholder')"
            size="lg"
            icon="i-lucide-credit-card"
            required
            minlength="3"
            maxlength="50"
            @update:model-value="emit('update', { documentId: $event })"
          />
        </UFormField>

        <CountrySelect
          :model-value="participant.nationality"
          :label="t('booking.nationality')"
          :placeholder="t('booking.nationality_placeholder')"
          size="lg"
          @update:model-value="handleNationalityChange($event || undefined)"
        />
      </div>

      <!-- Date of Birth -->
      <UFormField
        :label="t('booking.date_of_birth') || 'Fecha de Nacimiento'"
        name="dateOfBirth"
        :help="t('booking.date_of_birth_help')"
      >
        <UiDateInput
          :model-value="participant.dateOfBirth || null"
          size="lg"
          :max="getTodayString()"
          class="w-full"
          @update:model-value="emit('update', { dateOfBirth: $event || null })"
        />
      </UFormField>

      <!-- Pickup Address -->
      <UFormField
        :label="t('booking.pickup_address')"
        name="pickupAddress"
        required
        :help="t('booking.pickup_address_help')"
      >
        <UInput
          :model-value="participant.pickupAddress"
          :placeholder="t('booking.pickup_address_placeholder')"
          size="lg"
          icon="i-lucide-map-pin"
          required
          maxlength="200"
          :aria-label="t('aria.pickup_address')"
          @update:model-value="emit('update', { pickupAddress: $event })"
        />
      </UFormField>

      <!-- Special Requirements -->
      <UFormField
        :label="t('booking.special_requirements')"
        name="specialRequirements"
        :help="participant.specialRequirements ? `${participant.specialRequirements.length}/500` : undefined"
      >
        <UTextarea
          :model-value="participant.specialRequirements"
          :placeholder="t('booking.special_requirements_placeholder')"
          :rows="3"
          size="lg"
          class="w-full"
          maxlength="500"
          :aria-label="t('aria.special_requirements')"
          @update:model-value="emit('update', { specialRequirements: $event })"
        />
      </UFormField>

      <!-- Phone and Email -->
      <div class="grid md:grid-cols-2 gap-4">
        <UFormField
          :label="t('booking.phone')"
          name="phoneNumber"
          :help="t('booking.phone_help')"
        >
          <div class="flex gap-2">
            <select
              :value="participant.phoneCountryCode"
              class="w-28 px-2 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent text-sm"
              :aria-label="t('aria.country_code')"
              @change="emit('update', { phoneCountryCode: ($event.target as HTMLSelectElement).value })"
            >
              <option
                v-for="pc in phoneCodes"
                :key="pc.code + pc.country"
                :value="pc.code"
              >
                {{ getCountryFlag(pc.country) }} {{ pc.code }}
              </option>
            </select>
            <UInput
              :model-value="participant.phoneNumber"
              type="tel"
              placeholder="912345678"
              size="lg"
              icon="i-lucide-phone"
              class="flex-1"
              minlength="6"
              maxlength="15"
              :aria-label="t('aria.phone_number')"
              @update:model-value="emit('update', { phoneNumber: $event })"
            />
          </div>
        </UFormField>

        <UFormField
          :label="t('booking.email')"
          name="email"
          :help="t('booking.email_help')"
        >
          <UInput
            :model-value="participant.email"
            type="email"
            placeholder="tu@email.com"
            size="lg"
            icon="i-lucide-mail"
            class="w-full"
            maxlength="100"
            :aria-label="t('aria.email')"
            @update:model-value="emit('update', { email: $event })"
          />
        </UFormField>
      </div>

      <!-- Save options (only for logged-in users) -->
      <div
        v-if="isAuthenticated"
        class="pt-4 border-t border-neutral-200 dark:border-neutral-700 space-y-3"
      >
        <!-- Mark as Self checkbox -->
        <div
          v-if="canMarkAsSelf"
          class="flex items-center gap-2"
        >
          <UCheckbox
            :model-value="participant.markAsSelf"
            :label="t('booking.mark_as_self')"
            @update:model-value="handleMarkAsSelfChange($event)"
          />
          <UTooltip :text="t('booking.mark_as_self_tooltip')">
            <UIcon
              name="i-lucide-info"
              class="w-4 h-4 text-neutral-400"
            />
          </UTooltip>
        </div>

        <!-- Save for Future checkbox -->
        <div class="flex items-center gap-2">
          <UCheckbox
            :model-value="participant.saveForFuture"
            :label="t('booking.save_for_future')"
            @update:model-value="handleSaveForFutureChange($event)"
          />
        </div>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
import type { SavedParticipantRes } from 'api-client'
import { getTodayString } from '~/utils/dateUtils'

const { t } = useI18n()
const { phoneCodes, getPhoneCodeByCountry, getCountryFlag } = useCountries()
const authStore = useAuthStore()

const props = defineProps<{
  participant: {
    fullName: string
    documentId: string
    nationality: string
    dateOfBirth?: string | null
    pickupAddress: string
    specialRequirements: string
    phoneCountryCode: string
    phoneNumber: string
    email: string
    savedParticipantId?: string
    markAsSelf?: boolean
    saveForFuture?: boolean
  }
  index: number
  totalParticipants: number
  savedParticipants?: SavedParticipantRes[]
  hasSelfParticipantInOtherSlot?: boolean
}>()

const emit = defineEmits<{
  update: [data: Partial<{
    fullName: string
    documentId: string
    nationality: string
    dateOfBirth?: string | null
    pickupAddress: string
    specialRequirements: string
    phoneCountryCode: string
    phoneNumber: string
    email: string
    savedParticipantId?: string
    markAsSelf?: boolean
    saveForFuture?: boolean
  }>]
}>()

// Auth state
const isAuthenticated = computed(() => authStore.isAuthenticated)

// Saved participants
const savedParticipantOptions = computed(() => props.savedParticipants || [])

const selectedSavedParticipantId = computed(() => props.participant.savedParticipantId || '')

const savedParticipantSelectItems = computed(() => {
  const options = [
    { label: 'Ingresar manualmente', value: '' }
  ]

  savedParticipantOptions.value.forEach((p) => {
    options.push({
      label: p.isSelf ? `${p.fullName} (Yo)` : p.fullName,
      value: p.id
    })
  })

  return options
})

// Can mark as self: only if no other participant is already marked as self
// and if the user doesn't already have a self participant (unless this is it)
const canMarkAsSelf = computed(() => {
  // Don't show if another participant slot already has markAsSelf
  if (props.hasSelfParticipantInOtherSlot) {
    return false
  }

  // Show the checkbox
  return true
})

/**
 * Parse phone number to extract local number and country code.
 * Handles various formats: +56912345678, +56 9 1234 5678, 912345678, etc.
 */
function parsePhoneNumber(fullPhone: string | undefined | null): { countryCode: string, localNumber: string } {
  if (!fullPhone) {
    return { countryCode: '+56', localNumber: '' }
  }

  // Normalize: remove spaces and dashes
  const normalized = fullPhone.replace(/[\s-]/g, '')

  // Try to match international format: +XX... or +XXX...
  const intlMatch = normalized.match(/^(\+\d{1,3})(.*)$/)
  if (intlMatch) {
    const code = intlMatch[1] || '+56'
    const local = intlMatch[2] || ''
    // Find matching country code from our list
    const matchedCode = phoneCodes.find(pc => pc.code === code)
    return {
      countryCode: matchedCode ? code : '+56',
      localNumber: local
    }
  }

  // No country code prefix, return as-is
  return { countryCode: '+56', localNumber: normalized }
}

// Handle saved participant selection
function handleSavedParticipantSelect(selectedId: string | undefined) {
  if (!selectedId) {
    // Clear the saved participant reference
    emit('update', { savedParticipantId: undefined })
    return
  }

  const savedParticipant = savedParticipantOptions.value.find(p => p.id === selectedId)
  if (!savedParticipant) return

  // Parse phone number properly
  const { countryCode, localNumber } = parsePhoneNumber(savedParticipant.phoneNumber)

  // Pre-fill the form with saved participant data
  emit('update', {
    savedParticipantId: selectedId,
    fullName: savedParticipant.fullName,
    documentId: savedParticipant.documentId || '',
    nationality: savedParticipant.nationality || '',
    dateOfBirth: savedParticipant.dateOfBirth || null,
    phoneCountryCode: countryCode,
    phoneNumber: localNumber,
    email: savedParticipant.email || '',
    markAsSelf: savedParticipant.isSelf
  })
}

// Handle checkbox changes (UCheckbox can return 'indeterminate', so we convert to boolean)
function handleMarkAsSelfChange(value: boolean | 'indeterminate') {
  emit('update', { markAsSelf: value === true })
}

function handleSaveForFutureChange(value: boolean | 'indeterminate') {
  emit('update', { saveForFuture: value === true })
}

// When nationality changes, auto-update phone country code
function handleNationalityChange(nationality: string | undefined) {
  if (!nationality) {
    emit('update', { nationality: undefined })
    return
  }

  const phoneCode = getPhoneCodeByCountry(nationality)

  // Only auto-update phone code if user hasn't entered a phone number yet
  // or if the current code matches the previous nationality
  if (!props.participant.phoneNumber) {
    emit('update', {
      nationality,
      phoneCountryCode: phoneCode
    })
  } else {
    emit('update', { nationality })
  }
}
</script>
