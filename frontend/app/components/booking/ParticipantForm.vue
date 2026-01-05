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
        <DateInput
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
    </div>
  </UCard>
</template>

<script setup lang="ts">
import { getTodayString } from '~/utils/dateUtils'

const { t } = useI18n()
const { phoneCodes, getPhoneCodeByCountry, getCountryFlag } = useCountries()

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
  }
  index: number
  totalParticipants: number
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
  }>]
}>()

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
