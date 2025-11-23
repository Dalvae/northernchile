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
          @update:model-value="emit('update', { nationality: $event || undefined })"
        />
      </div>

      <!-- Date of Birth -->
      <UFormField
        :label="t('booking.date_of_birth') || 'Fecha de Nacimiento'"
        name="dateOfBirth"
        help="Usada para calcular la edad"
      >
        <UInput
          :model-value="participant.dateOfBirth || ''"
          type="date"
          placeholder="YYYY-MM-DD"
          size="lg"
          icon="i-lucide-calendar"
          :max="new Date().toISOString().split('T')[0]"
          aria-label="Fecha de nacimiento del participante"
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
          aria-label="Dirección de recogida"
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
          aria-label="Requerimientos especiales"
          @update:model-value="emit('update', { specialRequirements: $event })"
        />
      </UFormField>

      <!-- Phone and Email -->
      <div class="grid md:grid-cols-2 gap-4">
        <UFormField
          :label="t('booking.phone')"
          name="phoneNumber"
          help="Opcional. Para contactarte y enviarte fotos del tour"
        >
          <UInput
            :model-value="participant.phoneNumber"
            type="tel"
            placeholder="+56 9 1234 5678"
            size="lg"
            icon="i-lucide-phone"
            class="w-full"
            pattern="^\+?[0-9\s\-()]{7,20}$"
            minlength="7"
            maxlength="20"
            aria-label="Número de teléfono"
            @update:model-value="emit('update', { phoneNumber: $event })"
          />
        </UFormField>

        <UFormField
          :label="t('booking.email')"
          name="email"
          help="Opcional. Para enviarte fotos del tour"
        >
          <UInput
            :model-value="participant.email"
            type="email"
            placeholder="tu@email.com"
            size="lg"
            icon="i-lucide-mail"
            class="w-full"
            maxlength="100"
            aria-label="Correo electrónico"
            @update:model-value="emit('update', { email: $event })"
          />
        </UFormField>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
const { t } = useI18n()

defineProps<{
  participant: {
    fullName: string
    documentId: string
    nationality: string
    dateOfBirth?: string | null
    pickupAddress: string
    specialRequirements: string
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
    phoneNumber: string
    email: string
  }>]
}>()
</script>
